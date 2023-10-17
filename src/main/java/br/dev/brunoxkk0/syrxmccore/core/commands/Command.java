package br.dev.brunoxkk0.syrxmccore.core.commands;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    Class<? extends JavaPlugin> plugin();

    String command();

    String[] aliases() default {};

    String permission() default "";

    boolean playerOnly() default false;

    String usage() default "";

}
