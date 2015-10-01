package org.restler.client;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

/**
 * A CGLib implementation of {@link ClientFactory} that uses {@link CallExecutor} for execution client methods.
 */
@SuppressWarnings("unchecked")
public class CGLibClientFactory implements ClientFactory {

    private final CoreModule coreModule;

    public CGLibClientFactory(CoreModule coreModule) {
        this.coreModule = coreModule;
    }

    @Override
    public <C> C produceClient(Class<C> serviceDescriptor) {

        ClassServiceDescriptor descriptor = new ClassServiceDescriptor(serviceDescriptor);
        if (!coreModule.canHandle(descriptor)) {
            throw new RestlerException("Could not handle " + serviceDescriptor + " with " + coreModule);
        }
        InvocationHandler handler = coreModule.createHandler(descriptor);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(serviceDescriptor);
        enhancer.setCallback(handler);

        return (C) enhancer.create();
    }

}
