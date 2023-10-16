package br.dev.brunoxkk0.syrxmccore;


import br.dev.brunoxkk0.syrxmccore.core.ServerProperties;
import br.dev.brunoxkk0.syrxmccore.core.commands.CommandManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SyrxCore extends JavaPlugin {

    @Getter
    private static SyrxCore instance;

    @Getter
    private static CommandManager commandManager;

    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("Mundo principal setado como: " + ServerProperties.getInstance().getProperty("level-name"));
    }

    @Override
    public void onEnable() {

        getLogger().info("Ativando Modulo [Comandos]");

        CommandManager.packagesRegister(
                "br.dev.brunoxkk0.syrxmccore", this.getClass().getClassLoader()
        );

        Bukkit.getScheduler().runTask(this,
                (commandManager = CommandManager.getInstance()).register()
        );

    }

    @Override
    public void onDisable() {
        instance = null;
    }

}
