package org.restler.spring.mvc;

import org.restler.client.RestlerException;
import org.restler.spring.mvc.annotations.AnnotationProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AnnotationUtils {

    public static boolean isAnnotated(Class<?> clazz, String annotationName) {
        return findAnnotation(clazz, annotationName, new HashSet<>()) != null;
    }

    public static boolean isAnnotated(Method method, String annotationName) {
        for(Annotation annotation : method.getDeclaredAnnotations()) {
            if(findAnnotation(annotation.annotationType(), annotationName, new HashSet<>()) != null) {
                return true;
            }
        }

        return false;
    }

    public static <R extends AnnotationProxy> R getAnnotation(Class<?> clazz, Class<R> annotationClass) {
        String className = getClassName(annotationClass);

        Annotation annotation = findAnnotation(clazz, className, new HashSet<>());

        if(annotation != null) {
            return createAnnotationProxy(annotationClass, annotation);
        }

        return null;
    }

    public static <R extends AnnotationProxy> R getAnnotation(Method method, Class<R> annotationClass) {
        String className = getClassName(annotationClass);

        Annotation resultAnnotation;
        for(Annotation annotation : method.getDeclaredAnnotations()) {

            if(annotation.annotationType().getName().equals(className)) {
                return createAnnotationProxy(annotationClass, annotation);
            }

            if((resultAnnotation = findAnnotation(annotation.annotationType(), className, new HashSet<>())) != null) {
                return createAnnotationProxy(annotationClass, resultAnnotation);
            }
        }

        return null;
    }

    public static boolean instanceOf(Annotation annotation, String className) {
        return annotation.annotationType().getName().equals(className) || findAnnotation(annotation.annotationType(), className, new HashSet<>()) != null;
    }

    public static <R extends AnnotationProxy> R cast(Annotation annotation, Class<R> annotationClass) {
        String className = getClassName(annotationClass);

        if(instanceOf(annotation, className)) {
            return createAnnotationProxy(annotationClass, annotation);
        }

        throw new RestlerException("Can't cast " + annotation + " to " + annotationClass);
    }

    private static Annotation findAnnotation(Class<?> clazz, String annotationName, Set<Class<?>> set) {
        if(clazz == null || set.contains(clazz) || clazz.equals(Object.class)) {
            return null;
        }

        set.add(clazz);

        Annotation result = findAnnotation(clazz.getSuperclass(), annotationName, set);

        if(result != null) {
            return result;
        }

        Class<?>[] interfaces = clazz.getInterfaces();

        for(Class<?> interf : interfaces) {
            if(( result = findAnnotation(interf, annotationName, set)) != null) {
                return result;
            }
        }

        for(Annotation annotation : clazz.getDeclaredAnnotations()) {
            if(annotation.annotationType().getName().equals(annotationName)) {
                return annotation;
            } else if ((result = findAnnotation(annotation.annotationType(), annotationName, set)) != null) {
                return result;
            }
        }

        return null;
    }

    private static String getClassName(Class<? extends AnnotationProxy> annotationClass) {
        try {
            return annotationClass.getField("className").get(null).toString();
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RestlerException("Annotation class must have public static className field.", e);
        }
    }

    private static <R extends AnnotationProxy> R createAnnotationProxy(Class<R> annotationClass, Annotation annotation) {
        try {
            return annotationClass.getConstructor(Annotation.class).newInstance(annotation);
        }
        catch(InvocationTargetException e) {
            throw new RestlerException("Exception was threw by AnnotationProxy constructor.", e);
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RestlerException("Annotation class must have constructor that receive Annotation.", e);
        }
    }
}
