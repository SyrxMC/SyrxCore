package br.com.brunoxkk0.helper.events;

import br.com.brunoxkk0.SyrxCore;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.reflections.Reflections;

public class SEventHandler {

    private final Reflections reflections;

    public SEventHandler(){
        reflections = new Reflections("br.com.brunoxkk0");
    }

    public void scanEvents(){

        SyrxCore.getLoggerHelper().info("[EventHandler] » Starting events search.. ");
        int i = 0;

        for(Class<?> clazz : reflections.getSubTypesOf(Listener.class)){
            try{
                Bukkit.getPluginManager().registerEvents((Listener) clazz.newInstance(), SyrxCore.getInstance());
                i++;
            } catch (IllegalAccessException | InstantiationException ignored) { }
        }

        if(i > 0) {
            SyrxCore.getLoggerHelper().info("[EventHandler] » Registered " + i + " EventListener(s).");
            return;
        }

        SyrxCore.getLoggerHelper().info("[EventHandler] » Couldn't find any events..");
    }

}
