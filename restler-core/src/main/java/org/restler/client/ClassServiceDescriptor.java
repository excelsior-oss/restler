package org.restler.client;

public class ClassServiceDescriptor implements ServiceDescriptor {

    public final Class<?> serviceDescriptor;

    public ClassServiceDescriptor(Class<?> serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

}
