package plugin.artimc;

import plugin.artimc.common.AbstractManager;

import java.util.List;
import java.util.UUID;

public class PlayerChannelManager extends AbstractManager<UUID, Boolean> {
    private final List<String> commandOfChannels;
    private final String chatFormat;

    public PlayerChannelManager(ArtimcManager manager) {
        super(manager);
        this.chatFormat = getPlugin().getConfig().getString("chat.format");
        this.commandOfChannels = getPlugin().getConfig().getStringList("chat.leave-command");
    }

    public String getChatFormat() {
        return chatFormat;
    }

    public List<String> getCommandOfChannels() {
        return commandOfChannels;
    }
}
