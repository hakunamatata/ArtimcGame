package plugin.artimc.scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

/**
 * 描述：PlayerScoreboard，计分板管理器
 * 管理玩家的计分板显示
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 */
public class PlayerScoreboard {

    private final Scoreboard scoreboard;

    private final Objective objective;

    public static final String DEFAULT_TRAM_NAME = "game";

    private List<String> oldLines;

    private final Team team;

    public PlayerScoreboard(ScoreboardManager manager) {
        this.scoreboard = manager.getNewScoreboard();
        this.oldLines = new ArrayList<>();
        this.objective = scoreboard.registerNewObjective(DEFAULT_TRAM_NAME, "dummy", DEFAULT_TRAM_NAME);
        this.team = scoreboard.registerNewTeam(DEFAULT_TRAM_NAME);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * 设置玩家名称的颜色
     */
    public void setNameColor(NamedTextColor color) {
        this.team.color(color);
    }

    /**
     * 设置名称前缀
     *
     * @param prefix
     */
    public void setPrefix(String prefix) {
        this.team.prefix(Component.text(ChatColor.translateAlternateColorCodes('&', prefix)));
    }

    /**
     * Getter
     *
     * @return
     */
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    /**
     * Getter
     *
     * @return
     */
    public Objective getObjective() {
        return objective;
    }

    /**
     * Getter
     *
     * @return
     */
    public Team getTeam() {
        return team;
    }

    /**
     * 设置计分板标题
     *
     * @return
     */
    public void setTitle(String title) {
        getObjective().setDisplayName(title);
    }

    /**
     * 设置计分板内容
     *
     * @param lines
     */
    public void setLines(List<String> lines) {

        HashMap<String, Integer> add = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (add.containsKey(line)) { // if line has a duplicate name add §r
                while (add.containsKey(line))
                    line += ChatColor.RESET;
                lines.set(i, line);
            }
            add.put(line, lines.size() - i);
        }

        LinkedList<String> remove = new LinkedList<>();
        for (int i = 0; i < oldLines.size(); i++) {
            String line = oldLines.get(i);
            Integer newIndex = add.get(line);
            if (newIndex == null) // line no longer exists so remove it
                remove.add(line);
            else if (newIndex == oldLines.size() - i) // line already exists so don't add it
                add.remove(line);
        }

        oldLines = lines; // update list of old lines
        for (String line : remove)
            getScoreboard().resetScores(line);
        for (Map.Entry<String, Integer> line : add.entrySet())
            getObjective().getScore(line.getKey()).setScore(line.getValue());
    }
}
