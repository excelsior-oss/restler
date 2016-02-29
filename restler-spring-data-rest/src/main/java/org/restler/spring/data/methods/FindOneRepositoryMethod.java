package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;

import java.lang.reflect.Method;
import java.net.URI;


/**
 * CrudRepository findOne method implementation.
 */
public class FindOneRepositoryMethod extends DefaultRepositoryMethod {
    @Override
    public boolean isRepositoryMethod(Method method) {
        return "findOne".equals(method.getName());
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
