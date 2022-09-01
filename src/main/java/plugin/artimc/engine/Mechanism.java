package plugin.artimc.engine;

public class Mechanism extends AbstractMechanism {
    public Mechanism(IGame game) {
        super(game);
    }

    protected void remove() {
        getGame().getMechanisms().remove(this);
    }
}
