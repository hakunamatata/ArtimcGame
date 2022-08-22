package plugin.artimc.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.artimc.ArtimcGamePlugin;
import plugin.artimc.engine.Game;
import plugin.artimc.engine.GameTimer;
import plugin.artimc.engine.Party;
import plugin.artimc.utils.StringUtil;

public class GameManagerExpansion extends PlaceholderExpansion {

    private final ArtimcGamePlugin plugin;

    public GameManagerExpansion(ArtimcGamePlugin plugin) {
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

    /**
     * Placeholder 请求格式
     * %atg_desc_<游戏名称>% ：表示游戏的描述
     * %atg_onlines_<游戏名称>% ：表示游戏的在线人数
     * %atg_status_<游戏名称>% ：表示游戏的状态：等待中、进行中、已结束、已关闭
     * %atg_timeleft_<游戏名称>% ：表示游戏当前状态的剩余时间
     * %arg_players_<游戏名称>% ：表示当游戏当前的所有玩家
     *
     * @param player
     * @param params
     * @return
     */
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {

        if (params.startsWith("desc_")) {
            String gameName = params.replace("desc_", "");
            Game game = plugin.getManager().getGame(gameName);
            if (game != null) {
                return game.getGameMap().getDescription();
            }
            return "";
        }

        // 返回指定游戏内的在线玩家数
        if (params.startsWith("onlines_")) {
            String gameName = params.replace("onlines_", "");
            Game game = plugin.getManager().getGame(gameName);
            if (game != null) return String.valueOf(game.getOnlinePlayers().size());
            return String.valueOf(0);
        }

        // 返回指定游戏的状态
        if (params.startsWith("status_")) {
            String gameName = params.replace("status_", "");
            String status = "";
            Game game = plugin.getManager().getGame(gameName);
            if (game == null) status = "&7N/A";
            else {
                switch (game.getGameStatus()) {
                    case WAITING:
                        status = "&e等待中";
                        break;
                    case GAMING:
                        status = "&a进行中";
                        break;
                    case FINISH:
                        status = "&9结算中";
                        break;
                    case CLOSING:
                        status = "&c关闭中";
                        break;
                    default:
                        status = "&7已关闭";
                }
            }
            return ChatColor.translateAlternateColorCodes('&', status);
        }
        // 返回指定游戏当前状态的剩余时间
        if (params.startsWith("timeleft_")) {
            String gameName = params.replace("timeleft_", "");
            String timeLeft = "";
            Game game = plugin.getManager().getGame(gameName);
            if (game == null) return "N/A";
            GameTimer timer = null;
            switch (game.getGameStatus()) {
                case WAITING:
                    timer = game.getTimerManager().get(Game.WAIT_PERIOD_TIMER_NAME);
                    break;
                case GAMING:
                    timer = game.getTimerManager().get(Game.GAME_PERIOD_TIMER_NAME);
                    break;
                case FINISH:
                    timer = game.getTimerManager().get(Game.FINISH_PERIOD_TIMER_NAME);
                    break;
            }
            if (timer != null) return StringUtil.formatTime(timer.getCurrent());
        }

        // 获取游戏中的在线玩家名称
        if (params.startsWith("players_")) {
            String gameName = params.replace("players_", "");
            String playerNames = "";
            Game game = plugin.getManager().getGame(gameName);
            if (game == null) return "";
            for (Party party : game.getGameParties().values()) {
                String partyColor = ChatColor.WHITE + "";
                if (party != null && party.getPartyName() != null) partyColor = party.getPartyName().toString();
                for (Player pp : party.getOnlinePlayers()) {
                    playerNames += partyColor + pp.getName() + ";";
                }
            }
            return playerNames;
        }


        return "";
    }
}
