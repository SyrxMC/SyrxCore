package br.com.brunoxkk0.helper.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.List;

public interface ICommand {

    Plugin plugin();

    String getCommandName();
    String getCommandLabel();

    List<String> getAliases();

    String getPermission();

    default String getPermissionMessage(){
        return "&c&lVocê não pode executar esse comando, permissões insuficientes.";
    };

    TabCompleter getTabCompleter();
    String getUsage();

    String getDescription();

    String[] getRequiredPlugins();

    boolean process(CommandSender sender, String label, String[] args);

}
