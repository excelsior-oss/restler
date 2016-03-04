package org.restler.client;

/**
 * CoreModule is the main extension point of the library.
 * It produces a client from a controller class.
 */
public interface CoreModule {

    <C> C produceClient(Class<C> controllerClass) throws IllegalArgumentException;

}
