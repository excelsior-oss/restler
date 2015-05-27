package io.github.chcat.restclientgenerator.factory;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
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

class ControllerMethodInterceptor implements MethodInterceptor {

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private ControllerMethodExecutor executor;

    public ControllerMethodInterceptor(ControllerMethodExecutor executor){
        this.executor = executor;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        ControllerMethodExecutor.ControllerMethodDescription<?> description = getDescription(method);

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
        
        return executor.execute(description, requestBody, pathVariables,requestParams);
    }

    private static ControllerMethodExecutor.ControllerMethodDescription<?> getDescription(Method method){

        RequestMapping controllerMapping = method.getDeclaringClass().getDeclaredAnnotation(RequestMapping.class);
        RequestMapping methodMapping = method.getDeclaredAnnotation(RequestMapping.class);
        if (methodMapping == null){
            throw new RuntimeException("The method " + method + " is not mapped");
        }

        ResponseBody responseBodyAnnotation = method.getDeclaredAnnotation(ResponseBody.class);
        if (responseBodyAnnotation == null){
            responseBodyAnnotation = method.getDeclaringClass().getDeclaredAnnotation(ResponseBody.class);
        }

        if (responseBodyAnnotation == null){
            throw new RuntimeException("The method " + method + " does not return response body");
        }

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

        return new ControllerMethodExecutor.ControllerMethodDescription<>(uriTemplate,resultType, httpMethod, expectedStatus);
    }

    private static String getMappedUriString(RequestMapping mapping){
        if (mapping == null){
            return "";
        } else {
            return getFirstOrEmpty(mapping.value());
        }
    }

    private static String getFirstOrEmpty(String[] strings){
        if (strings == null || strings.length == 0){
            return "";
        } else {
            return strings[0];
        }
    }
}
