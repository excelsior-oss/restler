package io.github.chcat.restclientgenerator.factory;

/**
 * Created by pasa on 21.05.2015.
 */
public interface ClientFactory {
    <C> C produce(Class<C> type);
}
