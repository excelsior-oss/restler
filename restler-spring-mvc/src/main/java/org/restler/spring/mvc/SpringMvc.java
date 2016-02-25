package org.restler.spring.mvc;

import net.sf.cglib.proxy.InvocationHandler;
import org.restler.client.*;
import org.restler.http.HttpCallExecutor;
import org.restler.http.RequestExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

public class SpringMvc extends DefaultCoreModule {

    private final CallExecutionChain chain;
    private final URI baseUri;
    private final ParameterResolver parameterResolver;

    public SpringMvc(ClientFactory factory, RequestExecutor requestExecutor, List<CallEnhancer> enhancers, URI baseUri, ParameterResolver parameterResolver) {
        super(factory);
        HttpCallExecutor callExecutor = new HttpCallExecutor(requestExecutor);
        chain = new CallExecutionChain(callExecutor, enhancers);
        this.baseUri = baseUri;
        this.parameterResolver = parameterResolver;
    }

    @Override
    protected boolean canHandle(ServiceDescriptor descriptor) {
        if (!(descriptor instanceof ClassServiceDescriptor)) {
            return false;
        }
        Class<?> controllerClass = ((ClassServiceDescriptor) descriptor).getServiceDescriptor();
        return controllerClass.getDeclaredAnnotation(Controller.class) != null || controllerClass.getDeclaredAnnotation(RestController.class) != null;
    }

    @Override
    protected InvocationHandler createHandler(ServiceDescriptor descriptor) {

        return new CallExecutorInvocationHandler(chain, new SpringMvcMethodInvocationMapper(baseUri, parameterResolver));
    }

}
