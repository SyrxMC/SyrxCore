package br.dev.brunoxkk0.syrxmccore.core.commands;


import br.dev.brunoxkk0.syrxmccore.SyrxCore;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.command.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandManager implements CommandExecutor {


    private static CommandManager instance;

    public static CommandManager getInstance() {
        return instance != null ? instance : new CommandManager();
    }

    private CommandManager() {
        instance = this;
    }

    private static final HashMap<String, ClassLoader> targetPackages = new HashMap<>();

    public static void packagesRegister(String target) {
        if (target != null && !target.isEmpty())
            targetPackages.put(target, Thread.currentThread().getContextClassLoader());
    }

    public static void packagesRegister(String target, ClassLoader classLoader) {
        if (target != null && !target.isEmpty())
            targetPackages.put(target, classLoader);
    }

    public static void packagesUnregister(String target) {
        if (target != null && !target.isEmpty())
            targetPackages.remove(target);
    }


    private final LinkedHashMap<Command, Object> COMMANDS = new LinkedHashMap<>();
    private final LinkedHashMap<String, CompleteSupplier> COMPLETE_SUPPLIERS = new LinkedHashMap<>();


    @Getter
    @Setter
    public static boolean verbosePermissions = false;

    public void registerCompleteSupplier(String key, CompleteSupplier completeSupplier) {
        COMPLETE_SUPPLIERS.put(key, completeSupplier);
    }


    private void mapCommands() {

        ConfigurationBuilder configurationManager = new ConfigurationBuilder();

        for (Map.Entry<String, ClassLoader> pkg : targetPackages.entrySet()) {
            configurationManager = configurationManager.forPackage(pkg.getKey(), pkg.getValue());
        }

        Reflections reflections = new Reflections(configurationManager);

        Set<Class<?>> commandsExecutables = reflections.get(Scanners.SubTypes.of(CommandExecutable.class).asClass());
        Set<Class<?>> commands = reflections.get(Scanners.TypesAnnotated.with(Command.class).asClass());

        commandsExecutables.retainAll(commands);

        for (Class<?> clazz : commandsExecutables) {

            try {
                Command command = clazz.getDeclaredAnnotation(Command.class);
                COMMANDS.put(command, clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                SyrxCore.getInstance().getLogger().warning("[CommandManager] » Error when parsing a command: " + e);
            }

        }

    }

    private void load() {

        SyrxCore.getInstance().getLogger().info("[CommandManager] » Looking for commands...");

        mapCommands();

        SyrxCore.getInstance().getLogger().info("[CommandManager] » Found " + COMMANDS.size() + " command(s).");

        registerCommands();
        registerDefaultSuppliers();

        SyrxCore.getInstance().getLogger().info("[CommandManager] » Found " + COMPLETE_SUPPLIERS.size() + " complete suppliers.");

    }

    public void registerCommands() {

        try {

            @SuppressWarnings("JavaReflectionMemberAccess")
            Constructor<PluginCommand> pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCommandConstructor.setAccessible(true);

            for (Command command : COMMANDS.keySet()) {

                Plugin plugin = Arrays.stream(Bukkit.getPluginManager().getPlugins())
                        .filter(pl -> pl.getClass().equals(command.plugin()))
                        .findAny()
                        .orElse(SyrxCore.getInstance());

                PluginCommand pluginCommand = pluginCommandConstructor.newInstance(command.command(), plugin);

                pluginCommand.setExecutor(this);

                pluginCommand.setAliases(Arrays.asList(command.aliases()));

                pluginCommand.setLabel(command.command());
                pluginCommand.setUsage(command.usage());

                pluginCommand.setTabCompleter(tabCompleter(command));

                registerOnBukkit(pluginCommand);

            }

            if (!COMMANDS.isEmpty()) {
                SyrxCore.getInstance().getLogger().info("[CommandManager] » Commands registered.");
            }

        } catch (Exception e) {
            SyrxCore.getInstance().getLogger().info("[CommandHandler] » Failed to register commands.");
        }
    }

    private static CommandMap commandMap = null;

    public static CommandMap getCommandMap() {

        if (commandMap == null) {
            try {
                Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);

                commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());
            } catch (Exception e) {
                SyrxCore.getInstance().getLogger().warning("[CommandHandler] » Failed to instantiate the command map.");
            }
        }

        return commandMap;
    }

    public static Map<String, org.bukkit.command.Command> getCommandMapKnownCommands() {
        CommandMap commandMap = getCommandMap();
        try {
            Field field = Bukkit.getPluginManager().getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);
            //noinspection unchecked
            return (Map<String, org.bukkit.command.Command>) field.get(commandMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void registerOnBukkit(PluginCommand command) {

        try {

            unregisterCommand(command.getLabel(), command.getPlugin());

            for (String alias : command.getAliases())
                unregisterCommand(alias, command.getPlugin());

            getCommandMap().register(command.getPlugin().getName().toLowerCase(), command);
        } catch (Exception e) {
            SyrxCore.getInstance().getLogger().info("[CommandHandler] » Failed to register commands on bukkit. Command name: " + command.getName() + ", command class: " + command.getClass().getName() + ".");
        }

    }

    public static void unregisterCommand(String commandName, Plugin notifyPlugin) {

        try {

            Map<String, org.bukkit.command.Command> mapOfCommands = getCommandMapKnownCommands();
            org.bukkit.command.Command existingCommand = mapOfCommands.get(commandName);

            if (existingCommand == null) {
                return; //Command is not registered
            }

            mapOfCommands.remove(commandName);

            String originalPlugin = "BUKKIT";
            if (existingCommand instanceof PluginIdentifiableCommand pluginIdentifiableCommand) {

                Plugin plugin = pluginIdentifiableCommand.getPlugin();

                if (plugin != null) {
                    originalPlugin = "Plugin: " + plugin.getName();
                }

            }

            if (commandName.equals(existingCommand.getName())) {
                notifyPlugin.getLogger().warning("Removing existent command [" + existingCommand.getName() + "] from " + originalPlugin + "!");
            } else {
                notifyPlugin.getLogger().warning("Removing existent alias (" + commandName + ") for [" + existingCommand.getName() + "] from " + originalPlugin + "!");
            }

        } catch (Exception e) {
            SyrxCore.getInstance().getLogger().warning("Failed to UNREGISTER command [" + commandName + "] Message: " + e.getMessage());
        }
    }

    public static boolean noPermission(CommandSender sender, String permission) {
        String message = "§c[CommandHandler] » You don't have permission to execute this action.";

        if (verbosePermissions)
            message += String.format(" §c[§f{%s}§c].", permission);

        sender.sendMessage(message);
        return true;
    }

    public static boolean playerOnly(CommandSender sender) {
        sender.sendMessage("§c[CommandHandler] » This command can be only executed by players.");
        return true;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] strings) {

        Command cmd = COMMANDS.keySet().stream().filter(
                commandObjectEntry -> commandObjectEntry.command().equalsIgnoreCase(command.getName())
        ).findFirst().orElse(null);

        if (cmd != null) {

            if (cmd.playerOnly() && (commandSender instanceof ConsoleCommandSender))
                return playerOnly(commandSender);

            if (commandSender instanceof Player player) {
                if (!player.hasPermission(cmd.permission()))
                    return noPermission(player, cmd.permission());
            }

            CommandExecutable commandExecutable = (CommandExecutable) COMMANDS.get(cmd);
            commandExecutable.execute(new CommandContext(commandSender, label, strings));

            return true;

        }

        return false;
    }

    public BukkitRunnable register() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                load();
            }
        };
    }

    public TabCompleter tabCompleter(Command command) {

        return (commandSender, cmd, label, args) -> {

            if (commandSender instanceof Player player)
                if (!player.hasPermission(command.permission()))
                    return Collections.emptyList();

            if ((commandSender instanceof ConsoleCommandSender) && command.playerOnly())
                return Collections.emptyList();

            String usage = cmd.getUsage();

            if (usage.isEmpty())
                return Collections.emptyList();

            String[] tokens = usage.replaceAll("\\[", "<").replaceAll("]", ">").split(" ");
            tokens = Arrays.copyOfRange(tokens, 1, tokens.length);

            int currentPos;

            if (args == null || args.length == 0)
                currentPos = 0;
            else
                currentPos = args.length - 1;

            if (currentPos + 1 > tokens.length)
                return Collections.emptyList();

            String token = tokens[currentPos].toLowerCase().replaceAll("[<>]", "");

            Stream<String> stream = Stream.empty();

            CompleteSupplier completeSupplier;

            if (token.contains("|"))
                completeSupplier = COMPLETE_SUPPLIERS.get("|");
            else
                completeSupplier = COMPLETE_SUPPLIERS.get(token);

            try {
                if (completeSupplier != null)
                    stream = completeSupplier.generateHint(commandSender, cmd, label, currentPos, token, args);
            } catch (Exception exception) {
                SyrxCore.getInstance().getLogger().warning("Fail to calculate tab complete. Ex:" + exception.getMessage());
            }

            List<String> list = stream.collect(Collectors.toList());

            return list.isEmpty() ? Collections.emptyList() : list;
        };
    }

    private void registerDefaultSuppliers() {

        COMPLETE_SUPPLIERS.put("player", ((sender, command, label, currentPos, token, args) ->
                Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(el ->
                        isPartialMatch(el, currentPos, args)
                ))
        );

        COMPLETE_SUPPLIERS.put("offline_player", ((sender, command, label, currentPos, token, args) ->
                Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(el ->
                        isPartialMatch(el, currentPos, args)
                ))
        );

        COMPLETE_SUPPLIERS.put("item", ((sender, command, label, currentPos, token, args) ->
                Arrays.stream(Material.values()).map(el -> el.toString().toLowerCase()).filter(el ->
                        isPartialMatch(el, currentPos, args)
                ))
        );

        COMPLETE_SUPPLIERS.put("biomes", ((sender, command, label, currentPos, token, args) ->
                Arrays.stream(Biome.values()).map(el -> el.toString().toLowerCase()).filter(el ->
                        isPartialMatch(el, currentPos, args)
                ))
        );

        COMPLETE_SUPPLIERS.put("world", ((sender, command, label, currentPos, token, args) ->
                Bukkit.getWorlds().stream().map(World::getName).filter(el ->
                        isPartialMatch(el, currentPos, args)
                ))
        );

        COMPLETE_SUPPLIERS.put("particle", ((sender, command, label, currentPos, token, args) ->
                Arrays.stream(Particle.values()).map(el -> el.toString().toLowerCase()).filter(el ->
                        isPartialMatch(el, currentPos, args)
                ))
        );

        COMPLETE_SUPPLIERS.put("entity", ((sender, command, label, currentPos, token, args) ->
                Arrays.stream(EntityType.values()).map(el -> el.toString().toLowerCase()).filter(el ->
                        isPartialMatch(el, currentPos, args)
                ))
        );

        COMPLETE_SUPPLIERS.put("game_mode", ((sender, command, label, currentPos, token, args) ->
                Arrays.stream(GameMode.values()).map(el -> el.toString().toLowerCase()).filter(el ->
                        isPartialMatch(el, currentPos, args)
                ))
        );

        COMPLETE_SUPPLIERS.put("x", ((sender, command, label, currentPos, token, args) ->
                getLocation(sender).map(value ->
                        Stream.of(String.valueOf(value.getBlockX()))).orElseGet(() -> Stream.of(String.valueOf(0))
                ))
        );

        COMPLETE_SUPPLIERS.put("y", ((sender, command, label, currentPos, token, args) ->
                getLocation(sender).map(value ->
                        Stream.of(String.valueOf(value.getBlockY()))).orElseGet(() -> Stream.of(String.valueOf(0))
                ))
        );

        COMPLETE_SUPPLIERS.put("z", ((sender, command, label, currentPos, token, args) ->
                getLocation(sender).map(value ->
                        Stream.of(String.valueOf(value.getBlockZ()))).orElseGet(() -> Stream.of(String.valueOf(0))
                ))
        );

        COMPLETE_SUPPLIERS.put("|", ((sender, command, label, currentPos, token, args) ->
                Arrays.stream(token.split("\\|"))
        ));

    }

    private Optional<Location> getLocation(CommandSender commandSender) {

        if (commandSender instanceof Player player) {
            return Optional.of(player.getLocation());
        }

        return Optional.empty();
    }

    private boolean isPartialMatch(String element, int pos, String[] data) {

        if (data != null) {
            if (pos <= data.length - 1) {
                String target = data[pos];
                return target.isEmpty() || element.startsWith(data[pos]);
            }
        }

        return true;
    }

}