package org.restler.spring.mvc.annotations;

import org.restler.client.RestlerException;
import org.restler.http.HttpMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public class RequestMappingAnnotationProxy implements AnnotationProxy {

    public static final String className = "org.springframework.web.bind.annotation.RequestMapping";

    private final String name;
    private final String[] value;
    private final HttpMethod[] method;
    private final String[] params;
    private final String[] headers;
    private final String[] consumes;
    private final String[] produces;


    public RequestMappingAnnotationProxy(Annotation annotation) {
        try {
            name = (String)annotation.getClass().getMethod("name").invoke(annotation);
            value = (String[])annotation.getClass().getMethod("value").invoke(annotation);
            method = convertToHttpMethods((Object[])annotation.getClass().getMethod("method").invoke(annotation));
            params = (String[])annotation.getClass().getMethod("params").invoke(annotation);
            headers = (String[])annotation.getClass().getMethod("headers").invoke(annotation);
            consumes = (String[])annotation.getClass().getMethod("consumes").invoke(annotation);
            produces = (String[])annotation.getClass().getMethod("produces").invoke(annotation);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find the method.", e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RestlerException("Can't invoke the method.", e);
        }
    }

    public String name() {
        return name;
    }

    public String[] value() {
        return value;
    }

    public HttpMethod[] method() {
        return method;
    }

    public String[] params() {
        return params;
    }

    public String[] headers() {
        return headers;
    }

    public String[] consumes() {
        return consumes;
    }

    public String[] produces() {
        return produces;
    }

    private HttpMethod[] convertToHttpMethods(Object[] objects) {

        HttpMethod[] result = new HttpMethod[objects.length];

        for(Object object : objects) {
            HttpMethod.valueOf(object.toString());
        }

        return result;
    }

}
