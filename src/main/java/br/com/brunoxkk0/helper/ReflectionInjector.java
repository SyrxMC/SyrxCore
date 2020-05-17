package br.com.brunoxkk0.helper;

import br.com.brunoxkk0.injection.ClassInjector;

import java.io.IOException;

public class ReflectionInjector {

    public static boolean boot(ClassLoader classLoader){

        try {

            Class.forName("org.reflections.Reflections");

        } catch (ClassNotFoundException e) {

            try {

                new ClassInjector(classLoader,"reflections-0.9.12",0);
                new ClassInjector(classLoader,"javassist-3.26.0-GA",0);

                return true;
            } catch (IOException ex) {

                return false;
            }
        }

        return true;
    }

}
