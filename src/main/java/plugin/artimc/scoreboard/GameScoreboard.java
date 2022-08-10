package plugin.artimc.scoreboard;

import java.util.List;

import plugin.artimc.engine.Game;

/**
 * 描述：GameScoreboard，计分板管理器
 * 管理游戏的计分板显示
 * 作者：Leo
 * 创建时间：2022/7/29 20:40
 */
public class GameScoreboard extends BaseScoreboard {

    protected Game game;

    public GameScoreboard(Game game) {
        super(game.getPlugin());
        this.game = game;
    }

    public GameScoreboard(BaseScoreboard scoreboard, Game game) {
        super(scoreboard.getNames(), scoreboard.getScoreboards(), game.getPlugin());
        this.game = game;
    }

    @Override
    protected List<String> getLines() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getTitle() {
        // TODO Auto-generated method stub
        return null;
    }

}
