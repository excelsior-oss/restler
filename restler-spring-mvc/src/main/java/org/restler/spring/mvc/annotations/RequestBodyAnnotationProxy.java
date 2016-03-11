package org.restler.spring.mvc.annotations;

import org.restler.client.RestlerException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public class RequestBodyAnnotationProxy implements AnnotationProxy {

    public static final String className = "org.springframework.web.bind.annotation.RequestBody";

    private final boolean required;

    public RequestBodyAnnotationProxy(Annotation annotation) {
        try {
            required = (boolean)annotation.getClass().getMethod("required").invoke(annotation);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find the method.", e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RestlerException("Can't invoke the method.", e);
        }
    }

    public boolean required() {
        return required;
    }
}
