package org.restler.client;

import net.sf.cglib.proxy.InvocationHandler;

public interface CoreModule {

    boolean canHandle(ServiceDescriptor descriptor);

    InvocationHandler createHandler(ServiceDescriptor descriptor);

}
