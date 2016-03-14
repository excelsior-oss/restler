package org.restler.spring.mvc.asm;

import org.restler.client.RestlerException;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils {
    private static final Map<String, Class<?>> primitiveTypesMap = new HashMap<>(16);

    static {
        primitiveTypesMap.put("Z", boolean.class);
        primitiveTypesMap.put("B", byte.class);
        primitiveTypesMap.put("C", char.class);
        primitiveTypesMap.put("D", double.class);
        primitiveTypesMap.put("F", float.class);
        primitiveTypesMap.put("I", int.class);
        primitiveTypesMap.put("S", short.class);
        primitiveTypesMap.put("V", void.class);

        primitiveTypesMap.put("boolean", boolean.class);
        primitiveTypesMap.put("byte", byte.class);
        primitiveTypesMap.put("char", char.class);
        primitiveTypesMap.put("double", double.class);
        primitiveTypesMap.put("float", float.class);
        primitiveTypesMap.put("int", int.class);
        primitiveTypesMap.put("short", short.class);
        primitiveTypesMap.put("void", void.class);
    }

    public static Class<?> resolveClassName(String className, ClassLoader classLoader) {
        className = className.replace('/', '.');

        try {
            Class<?> primitiveType = primitiveTypesMap.get(className);

            if(primitiveType != null) {
                return primitiveType;
            }

            if(className.startsWith("L")) {
                return Class.forName(className.substring(1), true, classLoader);
            } else if(className.startsWith("[")) {
                return Array.newInstance(resolveClassName(className.substring(1), classLoader), 0).getClass();
            }

            throw new RestlerException("Can't resolve className " + className);
        } catch (ClassNotFoundException e) {
            throw new RestlerException("Can't found class " + className, e);
        }
    }

    public static boolean isPrimitiveType(String name) {
        return primitiveTypesMap.get(name) != null;
    }
}
