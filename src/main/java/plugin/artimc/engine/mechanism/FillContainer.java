package plugin.artimc.engine.mechanism;

import plugin.artimc.engine.IGame;
import plugin.artimc.engine.Mechanism;
import plugin.artimc.engine.resource.ResourceManager;
import plugin.artimc.engine.timer.GameTimer;
import plugin.artimc.engine.timer.custom.CustomTimer;

public class FillContainer extends Mechanism {

    private ResourceManager resourceManager;

    public FillContainer(IGame game) {
        super(game);
        resourceManager = new ResourceManager(game);
    }

    @Override
    public void onGameStart(IGame game) {
        resourceManager.addGameContainer();
        super.onGameStart(game);
    }

    @Override
    public void onGameTimerFinish(GameTimer timer) {
        if (timer.getName().equals(CustomTimer.RESOURCE_DROP_TIMER)) {
            resourceManager.addGameContainer();
        }
        super.onGameTimerFinish(timer);
    }
}
