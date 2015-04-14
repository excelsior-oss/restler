package io.github.chcat.restclientgenerator;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pavel Salimov on 17.02.2015.
 */
class ControllerMethodInterceptor implements MethodInterceptor {

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private ControllerMethodExecutor executor;
    private Class<?> type;
    private RequestMapping controllerMapping;
    private ResponseBody controllerBodyAnnotation;

    public ControllerMethodInterceptor(ControllerMethodExecutor executor){
        this.executor = executor;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        RequestMapping methodMapping = method.getDeclaredAnnotation(RequestMapping.class);
        if (methodMapping == null){
            throw new RuntimeException("The method " + method + " is not mapped");
        }

        ResponseBody methodBodyAnnotation = method.getDeclaredAnnotation(ResponseBody.class);
        if (methodBodyAnnotation == null){
            methodBodyAnnotation = controllerBodyAnnotation;
        }

        RequestMethod requestType = RequestMethod.GET;
        if (methodMapping.method() != null && methodMapping.method().length > 0){
            requestType = methodMapping.method()[0];
        }

        ResponseStatus expectedStatus = method.getDeclaredAnnotation(ResponseStatus.class);

        Object requestBody = null;
        Map<String, Object> pathVariables = new HashMap<>();
        Map<String, Object> requestParams = new HashMap<>();

        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        for (int pi = 0; pi < parametersAnnotations.length; pi++){
            for (int ai = 0; ai < parametersAnnotations[pi].length; ai++ ){
                Annotation annotation = parametersAnnotations[pi][ai];
                if (annotation instanceof PathVariable){

                    String pathVariableName = ((PathVariable) annotation).value();
                    if (StringUtils.isEmpty(pathVariableName)) pathVariableName = parameterNames[pi];
                    if (StringUtils.isEmpty(pathVariableName)) throw new RuntimeException("Name of a path variable can't be resolved during the method " + method +" call");

                    pathVariables.put(pathVariableName, args[pi]);

                } else if (annotation instanceof RequestParam){

                    String parameterVariableName = ((RequestParam) annotation).value();
                    if (StringUtils.isEmpty(parameterVariableName)) parameterVariableName = parameterNames[pi];
                    if (StringUtils.isEmpty(parameterVariableName)) throw new RuntimeException("Name of a request parameter can't be resolved during the method " + method +" call");

                    requestParams.put(parameterVariableName, args[pi]);

                } else if (annotation instanceof RequestBody){
                    requestBody = args[pi];
                }
            }
        }

        Class<?> resultType =  method.getReturnType();

        return executor.execute(controllerMapping,methodMapping,requestType,expectedStatus,requestBody,methodBodyAnnotation,resultType,pathVariables, requestParams);
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
