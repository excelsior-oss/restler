package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Created by rudenko on 11.02.2016.
 */
public interface CrudMethod {
    boolean isCrudMethod(Method method);

    Call getCall(Object[] args);
    Object getRequestBody(Object[] args);
    HttpMethod getHttpMethod();
    String getPathPart(Object[] args);
    ImmutableMultimap<String, String> getHeader();
}
