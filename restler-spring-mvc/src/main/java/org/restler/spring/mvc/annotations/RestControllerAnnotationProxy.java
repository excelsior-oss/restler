package org.restler.spring.mvc.annotations;

import java.lang.annotation.Annotation;

public class RestControllerAnnotationProxy implements AnnotationProxy {

    public static final String className = "org.springframework.web.bind.annotation.RestController";

    public RestControllerAnnotationProxy(Annotation annotation) {

    }
}
