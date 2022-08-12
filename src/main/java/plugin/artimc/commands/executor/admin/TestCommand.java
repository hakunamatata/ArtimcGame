package plugin.artimc.commands.executor.admin;

import plugin.artimc.commands.context.CommandContext;

import java.util.List;

public class TestCommand extends DefaultCommand {
    public TestCommand(CommandContext context) {
        super(context);
    }

    @Override
    public List<String> suggest() {
        return List.of();
    }

    @Override
    public boolean execute() {

        return super.execute();
    }
}
