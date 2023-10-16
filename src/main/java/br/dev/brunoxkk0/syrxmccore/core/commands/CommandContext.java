package br.dev.brunoxkk0.syrxmccore.core.commands;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.command.CommandSender;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CommandContext {

    private final CommandSender sender;
    private final String command;
    private final String[] args;

}
