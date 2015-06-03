package org.restler.client;

/**
 * ClientFactory implementations produce clients of the associated service, provided by annotated controller types.
 */
public interface ClientFactory {

    /**
     * Produces a client of the given controller type.
     *
     * @param controllerClass the type object of an annotated controller to make a client of
     * @param <C>             a type of the controller
     * @return an instance of the controller type that is a proxy delegating method execution to the service associated with the factory
     * @throws IllegalArgumentException if the controller type is final or is not annotated as a {@link org.springframework.stereotype.Controller}
     */
    <C> C produceClient(Class<C> controllerClass) throws IllegalArgumentException;
}
