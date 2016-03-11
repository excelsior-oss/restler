package org.restler.spring.mvc.annotations;

import java.lang.annotation.Annotation;

public class ControllerAnnotationProxy implements AnnotationProxy {

    public static final String className = "org.springframework.stereotype.Controller";

    public ControllerAnnotationProxy(Annotation annotation) {

    }
}
