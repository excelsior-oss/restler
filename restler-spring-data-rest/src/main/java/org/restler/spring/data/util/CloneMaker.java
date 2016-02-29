package org.restler.spring.data.util;

import org.restler.client.RestlerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CloneMaker {

    public static Object shallowClone(Object object) {
        Object clone;

        if(object instanceof Cloneable) {
            clone = cloneable((Cloneable) object);

            if(clone != null) {
                return clone;
            }
        }

        if((clone = byConstructor(object)) != null) {
            return clone;
        }

        if((clone = copyFields(object)) != null) {
            return clone;
        }

        throw new RestlerException("Can't clone object.");
    }

    private static Object cloneable(Cloneable cloneable) {
        try {
            Method cloneMethod = cloneable.getClass().getMethod("clone");

            return cloneMethod.invoke(cloneable);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }

    private static Object byConstructor(Object object) {
        Class<?> objectClass = object.getClass();
        try {
            return objectClass.getConstructor(objectClass).newInstance(object);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            return null;
        }
    }

    private static Object copyFields(Object object) {
        Class<?> objectClass = object.getClass();

        Object newInstance;

        try {
            Constructor constructor = objectClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            newInstance = constructor.newInstance();
            constructor.setAccessible(false);

            Field[] fields = objectClass.getDeclaredFields();

            for(Field field : fields) {
                field.setAccessible(true);
                field.set(newInstance, field.get(object));
                field.setAccessible(false);
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }

        return newInstance;

    }
}
