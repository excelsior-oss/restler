package org.restler.spring.mvc.annotations;

import org.restler.client.RestlerException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public class RequestParamAnnotationProxy implements AnnotationProxy {

    public static final String className = "org.springframework.web.bind.annotation.RequestParam";

    private final String value;
    private final boolean required;
    private final String defaultValue;

    public RequestParamAnnotationProxy(Annotation annotation) {
        try {
            value = (String)annotation.getClass().getMethod("value").invoke(annotation);
            required = (boolean)annotation.getClass().getMethod("required").invoke(annotation);
            defaultValue = (String)annotation.getClass().getMethod("defaultValue").invoke(annotation);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find the method.", e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RestlerException("Can't invoke the method.", e);
        }
    }

    public String value() {
        return value;
    }

    public boolean required() {
        return required;
    }

    public String defaultValue() {
        return defaultValue;
    }
}
