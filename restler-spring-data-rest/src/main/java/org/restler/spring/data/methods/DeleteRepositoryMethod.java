package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.proxy.ResourceProxy;

import java.lang.reflect.Method;
import java.net.URI;

public class DeleteRepositoryMethod extends DefaultRepositoryMethod {
    @Override
    public boolean isRepositoryMethod(Method method) {
        return "delete".equals(method.getName());
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
