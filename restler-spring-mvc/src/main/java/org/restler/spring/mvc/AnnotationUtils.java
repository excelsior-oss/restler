package org.restler.spring.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnnotationUtils {

    public static boolean isAnnotated(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return findAnnotation(clazz, annotationClass, new HashSet<>()) != null;
    }

    public static boolean isAnnotated(Method method, Class<? extends Annotation> annotationClass) {
        return Arrays.stream(method.getDeclaredAnnotations()).
                map((Annotation annotation)->findAnnotation(annotation.annotationType(), annotationClass, new HashSet<>())).
                filter((Annotation annotation)->annotation != null).
                count() > 0;
    }

    public static <R extends Annotation> R getAnnotation(Class<?> clazz, Class<R> annotationClass) {
        return (R)findAnnotation(clazz, annotationClass, new HashSet<>());
    }

    private static Annotation findAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass, Set<Class<?>> set) {
        if(clazz == null || set.contains(clazz) || clazz.equals(Object.class)) {
            return null;
        }

        set.add(clazz);

        Annotation result = findAnnotation(clazz.getSuperclass(), annotationClass, set);

        if(result != null) {
            return result;
        }

        Annotation findResult = Arrays.stream(clazz.getInterfaces()).
                map((Class<?> interf)->findAnnotation(interf, annotationClass, set)).
                filter((Annotation annotation)->annotation != null).
                findFirst().
                orElse(null);

        if(findResult != null) {
            return findResult;
        }

        Annotation interfaceAnnotation = Arrays.stream(clazz.getDeclaredAnnotations()).
                filter((Annotation annotation)->annotation.annotationType().equals(annotationClass)).
                findFirst().
                orElse(null);

        if(interfaceAnnotation != null) {
            return interfaceAnnotation;
        }

        return Arrays.stream(clazz.getDeclaredAnnotations()).
                map((Annotation annotation)->findAnnotation(annotation.annotationType(), annotationClass, set)).
                filter((Annotation annotation)->annotation != null).
                findFirst().
                orElse(null);
    }

}
