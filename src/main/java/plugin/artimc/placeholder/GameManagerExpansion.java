package plugin.artimc.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.artimc.*;
import plugin.artimc.common.IComponent;
import plugin.artimc.engine.IGame;
import plugin.artimc.engine.timer.GameTimer;
import plugin.artimc.engine.Party;
import plugin.artimc.engine.timer.TimerManager;
import plugin.artimc.engine.world.GameWorld;
import plugin.artimc.engine.world.WorldStatus;
import plugin.artimc.game.PvPGame;
import plugin.artimc.utils.StringUtil;

import java.util.logging.Logger;

public class GameManagerExpansion extends PlaceholderExpansion implements IComponent {

    private final ArtimcPlugin plugin;

    public GameManagerExpansion(ArtimcPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "atg";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Leo";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public ArtimcManager getManager() {
        return plugin.getManager();
    }

    @Override
    public GameManager getGameManager() {
        return getManager().getGameManager();
    }

    @Override
    public PlayerGameManager getPlayerGameManager() {
        return getManager().getPlayerGameManager();
    }

    @Override
    public PlayerPartyManager getPlayerPartyManager() {
        return getManager().getPlayerPartyManager();
    }

    @Override
    public PlayerChannelManager getPlayerChannelManager() {
        return getManager().getPlayerChannelManager();
    }

    @Override
    public WorldManager getWorldManager() {
        return getManager().getWorldManager();
    }

    @Override
    public Server getServer() {
        return getManager().getServer();
    }

    @Override
    public Logger getLogger() {
        return getManager().getLogger();
    }

    /**
     * Placeholder ????????????
     * %atg_desc_<????????????>% ????????????????????????
     * %atg_onlines_<????????????>% ??????????????????????????????
     * %atg_status_<????????????>% ????????????????????????????????????????????????????????????????????????
     * %atg_timeleft_<????????????>% ??????????????????????????????????????????
     * %atg_player_game% ????????????????????????????????????
     * %atg_party_owner% ?????????????????????
     * %atg_party_ready% ?????????????????????????????????
     * %atg_party_role% ????????????????????????host/guest
     *
     * @param player
     * @param params
     * @return
     */
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        if (params.startsWith("desc_")) {
            String gameName = params.replace("desc_", "");
            IGame game = getGameManager().get(gameName);
            if (game != null) {
                return game.getMap().getDescription();
            }
            return "";
        }

        // ???????????????????????????????????????
        if (params.startsWith("online_")) {
            String gameName = params.replace("online_", "");
            IGame game = getGameManager().get(gameName);
            if (game != null) return String.valueOf(game.getOnlinePlayers().size());
            return String.valueOf(0);
        }

        // ???????????????????????????
        if (params.startsWith("status_")) {
            String gameName = params.replace("status_", "");
            String status = "";
            String mapName = plugin.getGameConfigurations().get(gameName).getString("world-name");
            GameWorld world = plugin.getManager().getWorldManager().get(mapName);

            if (world != null && world.getStatus().equals(WorldStatus.RESET)) return "&c??????????????????";

            IGame game = getGameManager().get(gameName);
            if (game == null) status = "&7?????????";
            else if (game.isWaiting()) {
                status = "&e?????????";
            } else if (game.isGaming()) {
                status = "&a?????????";
            } else if (game.isFinish()) {
                status = "&9?????????";
            } else if (game.isClosing()) {
                status = "&9????????????";
            }
            return ChatColor.translateAlternateColorCodes('&', status);
        }
        // ?????????????????????????????????????????????
        if (params.startsWith("timeleft_")) {
            String gameName = params.replace("timeleft_", "");
            IGame game = getGameManager().get(gameName);
            if (game == null) return "N/A";
            GameTimer timer = null;

            if (game.isWaiting()) {
                timer = game.getTimerManager().get(TimerManager.WAIT_PERIOD_TIMER);
            } else if (game.isGaming()) {
                timer = game.getTimerManager().get(TimerManager.GAME_PERIOD_TIMER);
            } else if (game.isFinish()) {
                timer = game.getTimerManager().get(TimerManager.FINISH_PERIOD_TIMER);
            }

            if (timer != null) return StringUtil.formatTime(timer.getCurrent());
        }

        if (params.startsWith("player_game")) {
            IGame game = getPlayerGameManager().get(player.getUniqueId());
            if (game == null) return "";
            return game.getName();
        }

        if (params.startsWith("party_ready")) {
            IGame game = getPlayerGameManager().get(player.getUniqueId());
            if (game instanceof PvPGame) {
                PvPGame pvpGame = (PvPGame) game;
                if (isHostParty(player)) return pvpGame.isHostReady() ? "yes" : "no";
                if (isGuestParty(player)) return pvpGame.isGuestReady() ? "yes" : "no";
            }
            return "no";
        }

        if (params.startsWith("party_owner")) {
            Party party = getPlayerPartyManager().get(player.getUniqueId());
            if (party != null) return party.getOwnerName();
            return "";
        }

        if (params.startsWith("party_role")) {
            if (isHostParty(player)) return "host";
            if (isGuestParty(player)) return "guest";
            return "";
        }

        // ????????????????????????????????????
//        if (params.startsWith("players_")) {
//            String gameName = params.replace("players_", "");
//            String playerNames = "";
//            IGame game = getGameManager().get(gameName);
//            if (game == null) return "";
//            if (game instanceof PvPGame) {
//                PvPGame pvp = (PvPGame) game;
//                for (Party party : pvp.getGameParties().values()) {
//                    String partyColor = ChatColor.WHITE + "";
//                    if (party != null && party.getPartyName() != null) partyColor = party.getChatColor() + "";
//                    for (Player pp : party.getOnlinePlayers()) {
//                        playerNames += partyColor + pp.getName() + ";";
//                    }
//                }
//                return playerNames;
//            }
//        }
        return "";
    }

    private boolean isHostParty(Player player) {
        IGame game = getPlayerGameManager().get(player.getUniqueId());
        if (game instanceof PvPGame) {
            PvPGame pvpGame = (PvPGame) game;
            Party party = getPlayerPartyManager().get(player.getUniqueId());
            if (party != null && party.equals(pvpGame.getHostParty())) {
                return true;
            }
        }
        return false;
    }

    private boolean isGuestParty(Player player) {
        IGame game = getPlayerGameManager().get(player.getUniqueId());
        if (game instanceof PvPGame) {
            PvPGame pvpGame = (PvPGame) game;
            Party party = getPlayerPartyManager().get(player.getUniqueId());
            if (party != null && party.equals(pvpGame.getGuestParty())) {
                return true;
            }
        }
        return false;
    }
}
