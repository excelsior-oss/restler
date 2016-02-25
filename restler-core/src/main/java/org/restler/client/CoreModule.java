package org.restler.client;

/**
 * CoreModule it is main extension point of the library.
 * It produces client by class.
 */
public interface CoreModule {

    <C> C produceClient(Class<C> controllerClass) throws IllegalArgumentException;

}
