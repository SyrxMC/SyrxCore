package br.com.brunoxkk0.helper;

import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class LoggerHelper {

    private Plugin plugin;
    private boolean defaultLogger = false;

    private Logger logger;

    public LoggerHelper(Plugin plugin){

        this.plugin = plugin;

        try {
            logger = (Logger) plugin.getClass().getMethod("getSLF4JLogger").invoke(plugin, (Object) null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) { }

        defaultLogger = logger == null;

    }

    public void enableDefaultLogger(){
        defaultLogger = true;
    }

    public void disableDefaultLogger(){
        defaultLogger = false;
    }

    public void info(String message){

        if(plugin != null){

            if(!defaultLogger){
                logger.info(message);
            }else{
                plugin.getLogger().info(message);
            }

            return;
        }

        print(message, 0);
    }

    public void warn(String message){

        if(plugin != null){

            if(!defaultLogger){
                logger.warn(message);
            }else{
                plugin.getLogger().warning(message);
            }

            return;
        }

        print(message, 1);
    }

    public void error(String message){

        if(plugin != null){

            if(!defaultLogger){
                logger.error(message);
            }else{
                plugin.getLogger().warning(message);
            }

            return;
        }

        print(message, 2);
    }

    private void print(String message, int code){
        if(code == 0){
            System.out.println("[INFO] (LoggerHelper) " + message);
        }else if(code == 1){
            System.out.println("[WARN] (LoggerHelper) " + message);
        }else if(code == 2){
            System.out.println("[ERROR] (LoggerHelper) " + message);
        }else {
            System.out.println("[?] (LoggerHelper) " + message);
        }
    }

}
