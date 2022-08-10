package plugin.artimc.commands.executor.party;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plugin.artimc.commands.context.CommandContext;
import plugin.artimc.engine.Party;

public class CreateCommand extends DefaultCommand {

    public CreateCommand(CommandContext context) {
        super(context);
    }

    @Override
    public boolean execute() {
        // party create #name
        String partyName = tryGetArg(1);

        if (getParty() != null)
            throw new IllegalStateException(getLocaleString("command.ur-in-a-party-when-create"));

        Party party = new Party(getPlayer(), getPlugin());

        if (!partyName.isBlank()) {

            if (partyName.length() < 3 || partyName.length() > 16)
                throw new IllegalStateException("command.party-name-invalid");

            String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(partyName);
            partyName = m.replaceAll("").trim();
            party.setCustomName(partyName);
        }
        party.sendMessage(getLocaleString("command.party-created").replace("%party_custom_name%", partyName));
        return super.execute();
    }

    @Override
    public List<String> suggest() {
        return List.of();
    }

}
