package br.dev.brunoxkk0.syrxmccore.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.stream.Stream;

@FunctionalInterface
public interface CompleteSupplier {

    Stream<String> generateHint(CommandSender sender, Command command, String label, int currentPos, String token,  String ... args);

}
