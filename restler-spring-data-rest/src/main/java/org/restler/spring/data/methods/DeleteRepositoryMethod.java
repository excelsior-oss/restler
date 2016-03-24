package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.proxy.ResourceProxy;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;

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
            throw new RestlerException("Can't find CrudRepositoryMethod.delete method.", e);
        }
    }

    @Override
    public Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        return new HttpCall(uri, HttpMethod.DELETE, null, ImmutableMultimap.of("Content-Type", "application/json"), getRepositoryType(declaringClass).getActualTypeArguments()[0]);
    }

    @Override
    public String getPathPart(Object[] args) {
        Object arg;
        if(args.length == 1 && (arg = args[0]) instanceof ResourceProxy) {
            ResourceProxy resourceProxy = (ResourceProxy)arg;

            return resourceProxy.getResourceId().toString();
        }

        return "{id}";
    }
}
