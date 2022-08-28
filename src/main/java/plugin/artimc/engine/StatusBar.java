package plugin.artimc.engine;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class StatusBar {
    private final GameRunnable game;
    private final BossBar bar;
    private String title = "Untitled";
    private BarColor barColor;
    private BarStyle barStyle;
    private double progress = 1;
    private boolean visible = true;
    /**
     * 自动同步默认状态的进度
     */
    private boolean syncDefaultStatusBar = true;

    public StatusBar(GameRunnable gameRunnable) {
        this.game = gameRunnable;
        this.barColor = BarColor.RED;
        this.barStyle = BarStyle.SOLID;
        this.bar = game.getServer().createBossBar(title, barColor, barStyle);
        this.bar.setVisible(visible);
        this.bar.setProgress(progress);
    }

    public GameRunnable getGame() {
        return game;
    }

    public BossBar getBossBar() {
        return bar;
    }


    public boolean isSyncDefaultStatusBar() {
        return syncDefaultStatusBar;
    }

    public void setSyncDefaultStatusBar(boolean syncDefaultStatusBar) {
        this.syncDefaultStatusBar = syncDefaultStatusBar;
    }

    public void setTitle(String newTitle) {
        this.title = newTitle;
        this.bar.setTitle(newTitle);
    }

    public void setColor(BarColor barColor) {
        this.barColor = barColor;
        this.bar.setColor(barColor);
    }

    public void setStyle(BarStyle barStyle) {
        this.barStyle = barStyle;
        this.bar.setStyle(barStyle);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        this.bar.setVisible(visible);
    }

    public void setProgress(double progress) {
        this.progress = progress;
        this.bar.setProgress(progress);
    }
}
