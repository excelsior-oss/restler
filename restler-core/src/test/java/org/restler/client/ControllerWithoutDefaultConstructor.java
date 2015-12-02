package org.restler.client;

public class ControllerWithoutDefaultConstructor {

    private final Object someDependency;

    public ControllerWithoutDefaultConstructor(Object someDependency) {
        this.someDependency = someDependency;
    }

    void someMethod(String arg) {}
}
