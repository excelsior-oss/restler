package org.restler.spring.data.methods;

import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.chain.ChainCall;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * CrudRepository delete method implementation.
 */
public class DeleteRepositoryMethod extends DefaultRepositoryMethod {
    @Override
    public boolean isRepositoryMethod(Method method) {
        try {
            return CrudRepository.class.getMethod("delete", Object.class).equals(method) ||
                    CrudRepository.class.getMethod("delete", Serializable.class).equals(method);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.delete method.", e);
        }
    }

    @Override
    public Call getCall(URI uri, Class<?> declaringClass, Object[] args) {

        if(args.length == 1 && isIterable(args[0].getClass())) {
            Iterable<Object> objectsForDelete = (Iterable<Object>)args[0];

            List<Call> calls = new ArrayList<>();

            for(Object objectForDelete : objectsForDelete) {
                calls.add(new HttpCall(new UriBuilder(((ResourceProxy)objectForDelete).getSelfUri()).build(), HttpMethod.DELETE, null));
            }

            return new ChainCall(calls, void.class);
        }

        return new HttpCall(uri, HttpMethod.DELETE, null);
    }

    @Override
    public String getPathPart(Object[] args) {
        Object arg;
        if(args.length == 1 && isIterable(args[0].getClass())) {
            return "";
        }
        else if(args.length == 1 && (arg = args[0]) instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy)arg;

            return resourceProxy.getResourceId().toString();
        }

        return "{id}";
    }

    private boolean isIterable(Class<?> clazz) {
        if(clazz == null) {
            return false;
        }
        if(clazz.equals(Iterable.class)) {
            return true;
        }

        if (isIterable(clazz.getSuperclass())) {
            return true;
        }

        Class<?>[] interfaces = clazz.getInterfaces();

        for(Class<?> interf : interfaces) {
            if(isIterable(interf)) {
                return true;
            }
        }

        return false;
    }
}
