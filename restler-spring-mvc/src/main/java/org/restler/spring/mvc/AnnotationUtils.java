package org.restler.spring.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

class AnnotationUtils {

    static boolean isAnnotated(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return findAnnotation(clazz, annotationClass, new HashSet<>()) != null;
    }

    static boolean isAnnotated(Method method, Class<? extends Annotation> annotationClass) {
        return Arrays.stream(method.getDeclaredAnnotations()).
                map((Annotation annotation)->findAnnotation(annotation.annotationType(), annotationClass, new HashSet<>())).
                filter(Objects::nonNull).
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

        Optional<Annotation> findResult = Arrays.stream(clazz.getInterfaces()).
                map((Class<?> iface) -> findAnnotation(iface, annotationClass, set)).
                filter(Objects::nonNull).
                findFirst();
        if (findResult.isPresent()) {
            return findResult.get();
        }

        Optional<Annotation> interfaceAnnotation = Arrays.stream(clazz.getDeclaredAnnotations()).
                filter((Annotation annotation)->annotation.annotationType().equals(annotationClass)).
                findFirst();
        if (interfaceAnnotation.isPresent()) {
            return interfaceAnnotation.get();
        }

        return Arrays.stream(clazz.getDeclaredAnnotations()).
                map((Annotation annotation)->findAnnotation(annotation.annotationType(), annotationClass, set)).
                filter(Objects::nonNull).
                findAny().
                orElse(null);
    }

}
