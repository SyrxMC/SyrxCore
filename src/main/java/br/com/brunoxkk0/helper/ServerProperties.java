package br.com.brunoxkk0.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerProperties {

    private final Properties properties = new Properties();

    public ServerProperties(){
        try {
            properties.load(new FileInputStream(new File("server.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperties(String key){
        return properties.getProperty(key);
    }

    public String getProperties(String key, String defaultValue){
        return properties.getProperty(key, defaultValue);
    }

}
