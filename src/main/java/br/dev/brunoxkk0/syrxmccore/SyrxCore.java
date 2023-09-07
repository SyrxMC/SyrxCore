package br.dev.brunoxkk0.syrxmccore;


import br.dev.brunoxkk0.syrxmccore.core.ServerProperties;
import br.dev.brunoxkk0.syrxmccore.core.commands.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SyrxCore extends JavaPlugin {

    private static SyrxCore instance;

    private static CommandManager commandManager;

    public static SyrxCore getInstance() {
        return instance;
    }


    public static CommandManager getsCommandHandler() {
        return commandManager;
    }

    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("Mundo principal setado como: " + ServerProperties.getInstance().getProperty("level-name"));
    }

    @Override
    public void onEnable() {

        getLogger().info("Ativando Modulo [Comandos]");
        CommandManager.packagesRegister("br.dev.brunoxkk0.syrxmccore");
        Bukkit.getScheduler().runTask(this, (commandManager = CommandManager.getInstance()).register());

    }

    @Override
    public void onDisable() {
        instance = null;
    }

}
