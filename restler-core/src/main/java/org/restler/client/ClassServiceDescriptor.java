package org.restler.client;

public class ClassServiceDescriptor implements ServiceDescriptor {

    private final Class<?> serviceDescriptor;

    public ClassServiceDescriptor(Class<?> serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

    public Class<?> getServiceDescriptor() {
        return serviceDescriptor;
    }
}
