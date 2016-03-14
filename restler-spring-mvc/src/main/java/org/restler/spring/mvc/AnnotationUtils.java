package org.restler.spring.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class AnnotationUtils {

    public static boolean isAnnotated(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return findAnnotation(clazz, annotationClass, new HashSet<>()) != null;
    }

    public static boolean isAnnotated(Method method, Class<? extends Annotation> annotationClass) {
        for(Annotation annotation : method.getDeclaredAnnotations()) {
            if(findAnnotation(annotation.annotationType(), annotationClass, new HashSet<>()) != null) {
                return true;
            }
        }

        return false;
    }

    public static <R extends Annotation> R getAnnotation(Class<?> clazz, Class<R> annotationClass) {
        return (R)findAnnotation(clazz, annotationClass, new HashSet<>());
    }

    public static <R extends Annotation> R getAnnotation(Method method, Class<R> annotationClass) {
        Annotation resultAnnotation;
        for(Annotation annotation : method.getDeclaredAnnotations()) {

            if(annotation.annotationType().equals(annotationClass)) {
                return (R)annotation;
            }

            if((resultAnnotation = findAnnotation(annotation.annotationType(), annotationClass, new HashSet<>())) != null) {
                return (R)resultAnnotation;
            }
        }

        return null;
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

        Class<?>[] interfaces = clazz.getInterfaces();

        for(Class<?> interf : interfaces) {
            if(( result = findAnnotation(interf, annotationClass, set)) != null) {
                return result;
            }
        }

        for(Annotation annotation : clazz.getDeclaredAnnotations()) {
            if(annotation.annotationType().equals(annotationClass)) {
                return annotation;
            } else if ((result = findAnnotation(annotation.annotationType(), annotationClass, set)) != null) {
                return result;
            }
        }

        return null;
    }

}
