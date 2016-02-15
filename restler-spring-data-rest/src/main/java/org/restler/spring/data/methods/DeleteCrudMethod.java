package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.http.HttpMethod;
import org.restler.spring.data.proxy.ResourceProxy;

import java.lang.reflect.Method;

public class DeleteCrudMethod implements CrudMethod {
    @Override
    public boolean isCrudMethod(Method method) {
        return "delete".equals(method.getName());
    }

    @Override
    public Call getCall(Object[] args) {
        return null;
    }

    @Override
    public Object getRequestBody(Object[] args) {
        return null;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.DELETE;
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

    @Override
    public ImmutableMultimap<String, String> getHeader() {
        return ImmutableMultimap.of();
    }
}
