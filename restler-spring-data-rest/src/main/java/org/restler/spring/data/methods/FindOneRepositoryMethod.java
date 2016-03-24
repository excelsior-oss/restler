package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;


/**
 * CrudRepository findOne method implementation.
 */
public class FindOneRepositoryMethod extends DefaultRepositoryMethod {
    @Override
    public boolean isRepositoryMethod(Method method) {
        try {
            return CrudRepository.class.getMethod("findOne", Serializable.class).equals(method);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepositoryMethod.findOne method.", e);
        }
    }

    @Override
    public Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        return new HttpCall(uri, HttpMethod.GET, null, ImmutableMultimap.of("Content-Type", "application/json"), getRepositoryType(declaringClass).getActualTypeArguments()[0]);
    }

    @Override
    public String getPathPart(Object[] args) {
        return "{id}";
    }
}
