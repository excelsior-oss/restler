package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.http.HttpMethod;

import java.lang.reflect.Method;

/**
 * Created by rudenko on 11.02.2016.
 */
public class FindOneCrudMethod implements CrudMethod {
    @Override
    public boolean isCrudMethod(Method method) {
        return "findOne".equals(method.getName());
    }

    @Override
    public Object getRequestBody(Object[] args) {
        return null;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public String getPathSegment(Object[] args) {
        return "{id}";
    }

    @Override
    public ImmutableMultimap<String, String> getHeader() {
        return ImmutableMultimap.of();
    }
}
