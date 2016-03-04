package org.restler.client;

import net.sf.cglib.proxy.InvocationHandler;

/**
 * ClientFactory implementations produce clients of the associated service, provided by annotated controller types.
 */
public interface ClientFactory {

    /**
     * Produces a client of the given controller type.
     *
     * @param controllerClass the type object of an annotated controller to make a client for
     * @param handler the invocation handler is used for producing client proxy
     * @param <C>             the type of the controller
     * @return an instance of the controller type that is a proxy delegating method execution to the service associated with the factory
     */
    <C> C produceClient(Class<C> controllerClass, InvocationHandler handler) throws IllegalArgumentException;
}
