package br.com.brunoxkk0.helper;

import br.com.brunoxkk0.injection.ClassInjector;

import java.io.IOException;

public class ReflectionInjector {

    public static boolean boot(){

        try {

            Class.forName("org.reflections.Reflections");
            return true;

        } catch (ClassNotFoundException e) {

            try {
                new ClassInjector(ReflectionInjector.class.getClassLoader(),"reflections-0.9.10",0);
                new ClassInjector(ReflectionInjector.class.getClassLoader(),"javassist-3.18.2-GA",0);

                return true;
            } catch (IOException ex) {

                return false;
            }
        }
    }

}
