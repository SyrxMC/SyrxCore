package br.dev.brunoxkk0.syrxmccore.core.commands;


import br.dev.brunoxkk0.syrxmccore.SyrxCore;
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

    }

    public void registerCommands() {

        try {

            @SuppressWarnings("JavaReflectionMemberAccess")
            Constructor<PluginCommand> pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCommandConstructor.setAccessible(true);

            for (Command command : COMMANDS.keySet()) {

                PluginCommand pluginCommand = pluginCommandConstructor.newInstance(command.command(), SyrxCore.getInstance());

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

    public void registerOnBukkit(PluginCommand command) {
        try {

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());

            commandMap.register(command.getPlugin().getName().toLowerCase(), command);

        } catch (Exception e) {
            SyrxCore.getInstance().getLogger().info("[CommandHandler] » Failed to register commands on bukkit. Command name: " + command.getName() + ", command class: " + command.getClass().getName() + ".");
        }
    }

    public static boolean noPermission(CommandSender sender) {
        sender.sendMessage("§c[CommandHandler] » You don't have permission to execute this action.");
        return true;
    }

    public static boolean playerOnly(CommandSender sender) {
        sender.sendMessage("§c[CommandHandler] » This command can be only executed by players.");
        return true;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] strings) {

        Command cmd = COMMANDS.keySet().stream().filter(commandObjectEntry -> commandObjectEntry.command().equalsIgnoreCase(command.getName())).findFirst().orElse(null);

        if (cmd != null) {

            if (!cmd.consoleEnable() && (commandSender instanceof ConsoleCommandSender))
                return playerOnly(commandSender);

            Player player = (Player) commandSender;

            if (!player.hasPermission(cmd.permission()))
                return noPermission(player);

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

        return (commandSender, cmd, s, strings) -> {

            if (commandSender instanceof Player player)
                if (!player.hasPermission(command.permission()))
                    return Collections.emptyList();

            if ((commandSender instanceof ConsoleCommandSender) && !command.consoleEnable())
                return Collections.emptyList();

            String usage = cmd.getUsage();

            if (usage.isEmpty())
                return Collections.emptyList();

            String[] tokens = usage.replaceAll("\\[", "<").replaceAll("]", ">").split(" ");
            tokens = Arrays.copyOfRange(tokens, 1, tokens.length);

            int currentPos;

            if (strings == null || strings.length == 0)
                currentPos = 0;
            else
                currentPos = strings.length - 1;

            if (currentPos + 1 > tokens.length)
                return Collections.emptyList();

            String token = tokens[currentPos].toLowerCase().replaceAll("<", "").replaceAll(">", "");
            Stream<String> stream = Stream.empty();

            if (token.equals("player"))
                stream = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(el -> match(el, currentPos, strings));

            if (token.equals("offline_player"))
                stream = Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).filter(el -> match(el, currentPos, strings));

            if (token.equals("item"))
                stream = Arrays.stream(Material.values()).map(el -> el.toString().toLowerCase()).filter(el -> match(el, currentPos, strings));

            if (token.equals("biome"))
                stream = Arrays.stream(Biome.values()).map(el -> el.toString().toLowerCase()).filter(el -> match(el, currentPos, strings));

            if (token.equals("world"))
                stream = Bukkit.getWorlds().stream().map(World::getName).filter(el -> match(el, currentPos, strings));

            if (token.equals("particle"))
                stream = Arrays.stream(Particle.values()).map(el -> el.toString().toLowerCase()).filter(el -> match(el, currentPos, strings));

            if (token.equals("entity"))
                stream = Arrays.stream(EntityType.values()).map(el -> el.toString().toLowerCase()).filter(el -> match(el, currentPos, strings));

            if (token.equals("gamemode"))
                stream = Arrays.stream(GameMode.values()).map(el -> el.toString().toLowerCase()).filter(el -> match(el, currentPos, strings));

            if (token.contains("|")) {
                stream = Arrays.stream(token.split("\\|"));
            }

            if (token.equals("x")) {

                int x = 0;
                if (commandSender instanceof Player player) {
                    x = player.getLocation().getBlockX();
                }

                stream = Stream.of(String.valueOf(x));
            }

            if (token.equals("y")) {

                int y = 100;
                if (commandSender instanceof Player player) {
                    y = player.getLocation().getBlockY();
                }


                stream = Stream.of(String.valueOf(y));
            }

            if (token.equals("z")) {

                int z = 0;
                if (commandSender instanceof Player player) {
                    z = player.getLocation().getBlockZ();
                }

                stream = Stream.of(String.valueOf(z));
            }

            List<String> list = stream.collect(Collectors.toList());

            return list.isEmpty() ? Collections.emptyList() : list;
        };
    }

    private boolean match(String element, int pos, String[] data) {

        if (data != null) {
            if (pos <= data.length - 1) {
                String target = data[pos];
                return target.isEmpty() || element.startsWith(data[pos]);
            }
        }

        return true;
    }

}