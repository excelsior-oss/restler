package org.restler.spring.mvc.annotations;

import org.restler.client.RestlerException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public class PathVariableAnnotationProxy implements AnnotationProxy {

    public static final String className = "org.springframework.web.bind.annotation.PathVariable";

    private final String value;

    public PathVariableAnnotationProxy(Annotation annotation) {
        try {
            value = (String)annotation.getClass().getMethod("value").invoke(annotation);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find the method.", e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RestlerException("Can't invoke the method.", e);
        }
    }

    public String value() {
        return value;
    }
}
