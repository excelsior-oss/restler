package org.restler.client;

import net.sf.cglib.proxy.InvocationHandler;

public abstract class DefaultCoreModule implements CoreModule {

    private final ClientFactory factory;

    public DefaultCoreModule(ClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public <C> C produceClient(Class<C> controllerClass) throws IllegalArgumentException {
        ClassServiceDescriptor descriptor = new ClassServiceDescriptor(controllerClass);
        if (!canHandle(descriptor)) {
            throw new RestlerException("Could not handle " + controllerClass + " with " + this);
        }

        InvocationHandler handler = createHandler(descriptor);

        return factory.produceClient(controllerClass, handler);
    }

    protected abstract InvocationHandler createHandler(ServiceDescriptor descriptor);
}
