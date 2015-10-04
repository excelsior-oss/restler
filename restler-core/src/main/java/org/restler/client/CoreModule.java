package org.restler.client;

import net.sf.cglib.proxy.InvocationHandler;

/**
 * CoreModule it is main extension point of the library. Instances of core modules are responsible for service descriptions
 * parsing and producing instanced of {@code InvocationHandler} that will used to proxy remote services.
 */
public interface CoreModule {

    boolean canHandle(ServiceDescriptor descriptor);

    InvocationHandler createHandler(ServiceDescriptor descriptor);

}
