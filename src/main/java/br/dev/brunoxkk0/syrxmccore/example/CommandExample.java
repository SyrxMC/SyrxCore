package br.dev.brunoxkk0.syrxmccore.example;

import br.dev.brunoxkk0.syrxmccore.SyrxCore;
import br.dev.brunoxkk0.syrxmccore.core.commands.Command;
import br.dev.brunoxkk0.syrxmccore.core.commands.CommandContext;
import br.dev.brunoxkk0.syrxmccore.core.commands.CommandExecutable;

@Command(
        plugin = SyrxCore.class,
        command = "hello",
        aliases = {"hl", "hi"},
        consoleEnable = false,
        permission = "players.hello",
        usage = "/hello <player> <mine|craft> <x> <y> <z>"
)
public class CommandExample implements CommandExecutable {

    @Override
    public void execute(CommandContext commandContext) {
        String[] args = commandContext.getArgs();
        commandContext.getSender().sendMessage(String.join(",", args));
    }

}

