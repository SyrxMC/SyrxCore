package br.dev.brunoxkk0.syrxmccore.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerProperties {

    public static ServerProperties instance;

    public static ServerProperties getInstance() {
        return instance != null ? instance : new ServerProperties();
    }

    private final Properties properties = new Properties();

    private ServerProperties(){
        instance = this;
        try {
            properties.load(new FileInputStream("server.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key){
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue){
        return properties.getProperty(key, defaultValue);
    }

}
