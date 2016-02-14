package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Created by rudenko on 11.02.2016.
 */
public class FindOneCrudMethod implements CrudMethod {
    @Override
    public boolean isCrudMethod(Method method) {
        return "findOne".equals(method.getName());
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
        return HttpMethod.GET;
    }

    @Override
    public String getPathPart(Object[] args) {
        return "{id}";
    }

    @Override
    public ImmutableMultimap<String, String> getHeader() {
        return ImmutableMultimap.of();
    }
}
