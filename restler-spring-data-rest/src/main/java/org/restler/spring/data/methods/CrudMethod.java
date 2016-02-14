package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.http.HttpMethod;

import java.lang.reflect.Method;

/**
 * Created by rudenko on 11.02.2016.
 */
public interface CrudMethod {
    boolean isCrudMethod(Method method);
    Object getRequestBody(Object[] args);
    HttpMethod getHttpMethod();
    String getPathSegment(Object[] args);
    ImmutableMultimap<String, String> getHeader();
}
