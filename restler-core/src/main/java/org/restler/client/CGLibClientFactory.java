package org.restler.client;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import org.objenesis.ObjenesisStd;

/**
 * A CGLib implementation of {@link ClientFactory} that uses {@link CallExecutor} for execution of client methods.
 */
@SuppressWarnings("unchecked")
public class CGLibClientFactory implements ClientFactory {

    private final ObjenesisStd objenesis = new ObjenesisStd();

    @Override
    public <C> C produceClient(Class<C> serviceDescriptor, InvocationHandler handler) {
        Enhancer enhancer = new Enhancer();
        enhancer.setUseCache(false);
        enhancer.setSuperclass(serviceDescriptor);
        enhancer.setCallbackType(handler.getClass());

        Class aClass = enhancer.createClass();
        Enhancer.registerCallbacks(aClass, new Callback[] { handler });

        return (C) objenesis.newInstance(aClass);
    }

}
