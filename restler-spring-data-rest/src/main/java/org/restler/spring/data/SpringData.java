package org.restler.spring.data;

import net.sf.cglib.proxy.InvocationHandler;
import org.restler.RestlerConfig;
import org.restler.client.*;
import org.restler.http.HttpCallExecutor;
import org.restler.http.RequestExecutor;
import org.springframework.data.repository.Repository;

import java.net.URI;
import java.util.List;

public class SpringData implements CoreModule {

    private final URI baseUrl;
    private final HttpCallExecutor callExecutor;
    private final CallExecutionChain chain;

    private final Repositories repositories;

    public SpringData(URI baseUrl, RequestExecutor requestExecutor, List<CallEnhancer> enhancers, List<Class<?>> repositories) {
        this.baseUrl = baseUrl;
        callExecutor = new HttpCallExecutor(requestExecutor);
        chain = new CallExecutionChain(callExecutor, enhancers);

        this.repositories = new Repositories(repositories, new CachingClientFactory(new CGLibClientFactory(this)));
        this.repositories.initialize();
    }

    @Override
    public boolean canHandle(ServiceDescriptor descriptor) {
        return descriptor instanceof ClassServiceDescriptor && isRepository(((ClassServiceDescriptor) descriptor).getServiceDescriptor());
    }

    @Override
    public InvocationHandler createHandler(ServiceDescriptor descriptor) {
        return new CallExecutorInvocationHandler(chain, new SpringDataMethodInvocationMapper(baseUrl, repositories));
    }

    private boolean isRepository(Class<?> someClass) {
        if (someClass.isInterface()) {

            if (someClass == Repository.class) {
                return true;
            }

            Class<?>[] interfaces = someClass.getInterfaces();

            for (Class<?> interf : interfaces) {
                if (isRepository(interf)) {
                    return true;
                }
            }
        }
        return false;
    }
}
