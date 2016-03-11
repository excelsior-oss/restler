package org.restler.spring.mvc.annotations;

import java.lang.annotation.Annotation;

public class ResponseBodyAnnotationProxy implements AnnotationProxy {

    public static final String className = "org.springframework.web.bind.annotation.ResponseBody";

    public ResponseBodyAnnotationProxy(Annotation annotation) {

    }

}
