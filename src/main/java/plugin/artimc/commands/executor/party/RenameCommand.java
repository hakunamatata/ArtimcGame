package plugin.artimc.commands.executor.party;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plugin.artimc.commands.context.CommandContext;

public class RenameCommand extends DefaultCommand {

    public RenameCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        // party create #name
        String partyName = tryGetArg(1);
        if (getParty() == null)
            throw new IllegalStateException(getLocaleString("command.ur-not-in-a-party"));

        if (!getParty().isOwner(getPlayer()))
            throw new IllegalStateException(getLocaleString("command.ur-not-party-owner"));

        if (partyName.isBlank()) {
            getParty().setCustomName("");
            return true;
        }

        if (partyName.length() < 3 || partyName.length() > 16)
            throw new IllegalStateException("command.party-name-invalid");

        if (getGame() != null && getGame().isGaming()) 
            throw new IllegalStateException("game.rename-party-in-gaming");

        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(partyName);
        partyName = m.replaceAll("").trim();

        getParty().setCustomName(partyName);
        getParty().updateScoreboard();
        return super.execute();
    }

    @Override
    public List<String> suggest() {
        return List.of();
    }

}
