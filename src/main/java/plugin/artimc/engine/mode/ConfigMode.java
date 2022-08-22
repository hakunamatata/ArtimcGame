package plugin.artimc.engine.mode;

public class ConfigMode {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInvString() {
        return invString;
    }

    public void setInvString(String invString) {
        this.invString = invString;
    }

    private String name;
    private String invString;

    public ConfigMode(String mode, String invString) {
        this.name = mode;
        this.invString = invString;
    }

}
