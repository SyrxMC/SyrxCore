package br.com.brunoxkk0.injection;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassInjector {

    private ClassLoader classLoader;
    private FileLoader fileLoader;

    private Method addURL;

    public ClassInjector(ClassLoader classLoader, String path){
        this.classLoader = classLoader;
        this.fileLoader = new FileLoader(path);

        try {
            addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        setup();
    }

    public ClassInjector(ClassLoader classLoader, String name, int ignored) throws IOException {
        this.classLoader = classLoader;

        try {
            addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        setupUrl(name);
    }

    private void setup(){

        for(File file : fileLoader.getFiles()){

            String name = file.getName();

            try {
                addURL.invoke(classLoader, file.toURI().toURL());

                System.out.println("[ClassInjector] Loaded classes from: " + name);
            } catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
                System.out.println("[ClassInjector] Fail to load classes from: " + name);

            }

        }

    }

    private static File toFile(InputStream in, String name, String suffix) throws IOException {
        final File tempFile = File.createTempFile(name, suffix);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }

    private void setupUrl(String name) throws IOException {

        InputStream is =  this.getClass().getResourceAsStream("/libs/" + name + ".jar");

        if(is != null){

            File file = toFile(is, name,".jar");

            if(file.exists()){

                String Fname = file.getName(); Fname = Fname.substring(0, (Fname.length() - 23) );

                try {
                    addURL.invoke(classLoader, file.toURI().toURL());

                    System.out.println("[ClassInjector] Loaded classes from: " + Fname);
                } catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
                    System.out.println("[ClassInjector] Fail to load classes from: " + Fname);

                }
            }
        }
    }

}
