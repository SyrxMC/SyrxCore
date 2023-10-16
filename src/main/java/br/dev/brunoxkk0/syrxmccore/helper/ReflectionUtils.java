package br.dev.brunoxkk0.syrxmccore.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {

    public static <T> T createInstance(Class<T> target) throws InvocationTargetException, InstantiationException, IllegalAccessException {

        Constructor<?>[] constructors = target.getDeclaredConstructors();
        Constructor<?> finalConstructor = null;

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                finalConstructor = constructor;
                break;
            }

        }
        if (finalConstructor != null) {

            finalConstructor.setAccessible(true);

            //noinspection unchecked
            return (T) finalConstructor.newInstance();
        }

        throw new IllegalArgumentException(String.format("The target class %s don't expose an a no args constructor.", target.getName()));
    }

}
