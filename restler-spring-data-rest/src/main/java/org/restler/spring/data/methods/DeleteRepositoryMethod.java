package org.restler.spring.data.methods;

import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.calls.ChainCall;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * CrudRepository delete method implementation.
 */
public class DeleteRepositoryMethod extends DefaultRepositoryMethod {

    private static final Method deleteObjectMethod;
    private static final Method deleteSerializableMethod;

    static {
        try {
            deleteObjectMethod = CrudRepository.class.getMethod("delete", Object.class);
            deleteSerializableMethod = CrudRepository.class.getMethod("delete", Serializable.class);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.delete method.", e);
        }
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        return deleteObjectMethod.equals(method) || deleteSerializableMethod.equals(method);
    }

    @Override
    public Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        if(args.length == 1 && isIterable(args[0].getClass())) {
            Iterable<Object> objectsForDelete = (Iterable<Object>)args[0];

            List<Call> calls = StreamSupport.stream(objectsForDelete.spliterator(), false).
                    filter(o -> o instanceof ResourceProxy).
                    map(r -> makeDeleteCall((ResourceProxy) r)).
                    collect(Collectors.toList());

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

    private HttpCall makeDeleteCall(ResourceProxy resource) {
        return new HttpCall(new UriBuilder((resource).getSelfUri()).build(), HttpMethod.DELETE, null);
    }

    private boolean isIterable(Class<?> clazz) {
        return clazz != null &&
                (clazz.equals(Iterable.class) ||
                isIterable(clazz.getSuperclass()) ||
                Arrays.stream(clazz.getInterfaces()).anyMatch(this::isIterable));
    }
}
