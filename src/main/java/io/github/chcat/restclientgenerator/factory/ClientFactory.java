package io.github.chcat.restclientgenerator.factory;

public interface ClientFactory {
    <C> C produce(Class<C> type);
}
