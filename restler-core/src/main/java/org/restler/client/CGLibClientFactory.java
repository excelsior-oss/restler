package org.restler.client;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import org.objenesis.ObjenesisStd;

/**
 * A CGLib implementation of {@link ClientFactory} that uses {@link CallExecutor} for execution client methods.
 */
@SuppressWarnings("unchecked")
public class CGLibClientFactory implements ClientFactory {

    private final ObjenesisStd objenesis = new ObjenesisStd();
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
        enhancer.setCallbackType(handler.getClass());

        Class aClass = enhancer.createClass();
        Enhancer.registerCallbacks(aClass, new Callback[] { handler });

        return (C) objenesis.newInstance(aClass);
    }

}
