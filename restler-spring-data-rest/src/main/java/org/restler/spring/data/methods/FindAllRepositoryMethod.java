package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.util.ArrayListType;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;

public class FindAllRepositoryMethod extends DefaultRepositoryMethod {

    @Override
    protected Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        Type itemType = getRepositoryType(declaringClass).getActualTypeArguments()[0];
        return new HttpCall(uri, HttpMethod.GET, null, ImmutableMultimap.of("Content-Type", "application/json"), new ArrayListType(itemType));
    }

    @Override
    protected String getPathPart(Object[] args) {
        return "";
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        try {
            return CrudRepository.class.getMethod("findAll").equals(method);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.findAll method.", e);
        }
    }
}
