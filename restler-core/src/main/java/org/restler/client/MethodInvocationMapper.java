package org.restler.client;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Maps a class method invocation to a service method invocation.
 */
public class MethodInvocationMapper implements BiFunction<Method, Object[], ServiceMethodInvocation<?>> {

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    @Override
    public ServiceMethodInvocation<?> apply(Method method, Object[] args) {
        ServiceMethodDescription<?> description = getDescription(method);

        Object requestBody = null;
        Map<String, Object> pathVariables = new HashMap<>();
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();

        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        for (int pi = 0; pi < parametersAnnotations.length; pi++){
            for (int ai = 0; ai < parametersAnnotations[pi].length; ai++ ){
                Annotation annotation = parametersAnnotations[pi][ai];
                if (annotation instanceof PathVariable){

                    String pathVariableName = ((PathVariable) annotation).value();
                    if (StringUtils.isEmpty(pathVariableName) && parameterNames != null) pathVariableName = parameterNames[pi];
                    if (StringUtils.isEmpty(pathVariableName)) throw new RuntimeException("Name of a path variable can't be resolved during the method " + method +" call");

                    pathVariables.put(pathVariableName, args[pi]);

                } else if (annotation instanceof RequestParam){

                    String parameterVariableName = ((RequestParam) annotation).value();
                    if (StringUtils.isEmpty(parameterVariableName) && parameterNames != null) parameterVariableName = parameterNames[pi];
                    if (StringUtils.isEmpty(parameterVariableName)) throw new RuntimeException("Name of a request parameter can't be resolved during the method " + method +" call");

                    requestParams.add(parameterVariableName, args[pi].toString());

                } else if (annotation instanceof RequestBody){
                    requestBody = args[pi];
                }
            }
        }

        return new ServiceMethodInvocation<>(description, requestBody, pathVariables,requestParams);
    }

    private ServiceMethodDescription<?> getDescription(Method method){

        RequestMapping controllerMapping = method.getDeclaringClass().getDeclaredAnnotation(RequestMapping.class);
        RequestMapping methodMapping = method.getDeclaredAnnotation(RequestMapping.class);
        if (methodMapping == null){
            throw new RuntimeException("The method " + method + " is not mapped");
        }

//            ResponseBody responseBodyAnnotation = AnnotationUtils.findAnnotation(method, ResponseBody.class);
//            if (responseBodyAnnotation == null){
//                throw new RuntimeException("The method " + method + " does not return response body");
//            }

        RequestMethod declaredMethod;
        if (methodMapping.method() == null || methodMapping.method().length == 0) {
            declaredMethod = RequestMethod.GET;
        } else {
            declaredMethod = methodMapping.method()[0];
        }
        HttpMethod httpMethod = HttpMethod.valueOf(declaredMethod.toString());

        HttpStatus expectedStatus = HttpStatus.OK;
        ResponseStatus statusAnnotation = method.getDeclaredAnnotation(ResponseStatus.class);
        if (statusAnnotation != null) {
            expectedStatus = statusAnnotation.value();
        }

        String uriTemplate = UriComponentsBuilder.fromUriString("/").pathSegment(getMappedUriString(controllerMapping), getMappedUriString(methodMapping)).build().toUriString();

        Class<?> resultType =  method.getReturnType();

        return new ServiceMethodDescription<>(uriTemplate,resultType, httpMethod, expectedStatus);
    }

    private String getMappedUriString(RequestMapping mapping){
        if (mapping == null){
            return "";
        } else {
            return getFirstOrEmpty(mapping.value());
        }
    }

    private String getFirstOrEmpty(String[] strings){
        if (strings == null || strings.length == 0){
            return "";
        } else {
            return strings[0];
        }
    }
}
