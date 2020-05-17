package br.com.brunoxkk0.helper.commands;


import br.com.brunoxkk0.SyrxCore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class SCommandHandler implements CommandExecutor {

    private final ArrayList<ICommand> commandslist = new ArrayList<>();
    private final Reflections reflections;

    public SCommandHandler(){
        reflections = new Reflections("br.com.brunoxkk0");
    }

    public void setup(){

        SyrxCore.getLoggerHelper().info("[CommandHandler] » Starting command search..");
        scanCommands();

        if(!commandslist.isEmpty()){
            SyrxCore.getLoggerHelper().info("[CommandHandler] » Founded " + commandslist.size() + " command(s), registering...");
            registerCommands();
            return;
        }

        SyrxCore.getLoggerHelper().info("[CommandHandler] » Couldn't find any commands..");
    }


    public void scanCommands(){

        for(Class<?> clazz : reflections.getTypesAnnotatedWith(Command.class)){

            try {
                ICommand command = (ICommand) clazz.newInstance();

                boolean deps = true;

                for(String requiredPlugin : command.getRequiredPlugins()){
                    if(!Bukkit.getPluginManager().isPluginEnabled(requiredPlugin)) deps = false; break;
                }

                if(deps && command.getCommandName() != null && command.plugin() != null && command.getCommandLabel() != null){

                    if(Bukkit.getPluginManager().isPluginEnabled(command.plugin()))
                        commandslist.add(command);

                }

            } catch (IllegalAccessException | InstantiationException e) {
                SyrxCore.getLoggerHelper().error("[CommandHandler] » Failed to parse " + clazz.getName() + " as a command.");
            }

        }

    }

    public void registerCommands() {

        try{
            Constructor<PluginCommand> pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCommandConstructor.setAccessible(true);

            for(ICommand command : commandslist){
                PluginCommand pluginCommand = pluginCommandConstructor.newInstance(command.getCommandLabel(), command.plugin());

                pluginCommand.setExecutor(this);
                pluginCommand.setAliases(command.getAliases());
                pluginCommand.setLabel(command.getCommandLabel());
                pluginCommand.setTabCompleter(command.getTabCompleter());
                pluginCommand.setPermission(command.getPermission());
                pluginCommand.setPermissionMessage(command.getPermissionMessage());
                pluginCommand.getUsage();
                pluginCommand.getDescription();

                registerOnBukkit(pluginCommand);

            }

        }catch (Exception e){
            SyrxCore.getLoggerHelper().info("[CommandHandler] » Failed to register commands.");
        }
    }

    public void registerOnBukkit(PluginCommand command){
        try{

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());

            commandMap.register(command.getPlugin().getName().toLowerCase(), command);

        }catch (Exception e){
            SyrxCore.getLoggerHelper().info("[CommandHandler] » Failed to register commands on bukkit. Command name:  " + command.getName() + ", command class: " + command.getClass().getName() + ".");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {

        ICommand cmd = null;

        for(ICommand iCommand : commandslist){
            if(command.getName().equals(iCommand.getCommandLabel())){
                cmd = iCommand;
                break;
            }
        }

        if(cmd != null){

            if((commandSender instanceof Player) && !commandSender.hasPermission(cmd.getPermission())){
                commandSender.sendMessage(cmd.getPermissionMessage().replace("&","\u00a7"));
                return false;
            }

            return cmd.process(commandSender, s, strings);
        }

        return false;
    }
}
