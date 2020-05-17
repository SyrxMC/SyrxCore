package br.com.brunoxkk0;


import br.com.brunoxkk0.helper.LoggerHelper;
import br.com.brunoxkk0.helper.ReflectionInjector;
import br.com.brunoxkk0.helper.ServerProperties;
import br.com.brunoxkk0.helper.commands.SCommandHandler;
import br.com.brunoxkk0.helper.events.SEventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;


public class SyrxCore extends JavaPlugin implements Listener {

    private static SyrxCore instance;

    private static final ServerProperties SERVER_PROPERTIES = new ServerProperties();
    private static SCommandHandler sCommandHandler;
    private static SEventHandler sEventHandler;
    private static LoggerHelper loggerHelper;

    public static SyrxCore getInstance() {
        return instance;
    }

    public static LoggerHelper getLoggerHelper() {
        return loggerHelper;
    }

    public static ServerProperties getServerProperties(){
        return SERVER_PROPERTIES;
    }

    public static SCommandHandler getsCommandHandler(){
        return sCommandHandler;
    }

    public static SEventHandler getsEventHandler() {
        return sEventHandler;
    }

    @Override
    public void onLoad() {

        instance = this;
        loggerHelper = new LoggerHelper(this);

        getLoggerHelper().info("Mundo principal setado como: " + getServerProperties().getProperties("level-name"));

        if(ReflectionInjector.boot()){
            getLoggerHelper().info("Iniciando [Reflections - 0.9.12]");
        }

    }

    @Override
    public void onEnable() {

        getLoggerHelper().info("Ativando Modulo [Comandos]");
        (sCommandHandler = new SCommandHandler()).setup();

        getLoggerHelper().info("Ativando Modulo [Eventos]");
        (sEventHandler = new SEventHandler()).scanEvents();

    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public void disable(){
        this.getPluginLoader().disablePlugin(this);
    }

}
