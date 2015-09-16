package org.restler.spring;

import net.sf.cglib.proxy.InvocationHandler;
import org.restler.ServiceBuilder;
import org.restler.client.*;
import org.restler.http.HttpCallExecutor;
import org.restler.http.RequestExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class SpringMvc implements CoreModule {

    private final RequestExecutor requestExecutor;
    private final List<CallExecutionAdvice<?>> enhancers;
    private final URI baseUri;
    private final ParameterResolver parameterResolver;

    public SpringMvc(RequestExecutor requestExecutor, List<CallExecutionAdvice<?>> enhancers, URI baseUri, ParameterResolver parameterResolver) {
        this.requestExecutor = requestExecutor;
        this.baseUri = baseUri;
        this.parameterResolver = parameterResolver;

        this.enhancers = new ArrayList<>();
        this.enhancers.addAll(enhancers);
        this.enhancers.addAll(singletonList(new DeferredResultHandler(ServiceBuilder.restlerExecutor)));
    }

    @Override
    public boolean canHandle(ServiceDescriptor descriptor) {
        if (!(descriptor instanceof ClassServiceDescriptor)) {
            return false;
        }
        Class<?> controllerClass = ((ClassServiceDescriptor) descriptor).serviceDescriptor;
        return controllerClass.getDeclaredAnnotation(Controller.class) != null || controllerClass.getDeclaredAnnotation(RestController.class) != null;
    }

    @Override
    public InvocationHandler createHandler(ServiceDescriptor descriptor) {
        HttpCallExecutor callExecutor = new HttpCallExecutor(requestExecutor);
        CallExecutionChain chain = new CallExecutionChain(callExecutor, enhancers);
        return new CallExecutorInvocationHandler(chain, new ControllerMethodInvocationMapper(baseUri, parameterResolver));
    }

}
