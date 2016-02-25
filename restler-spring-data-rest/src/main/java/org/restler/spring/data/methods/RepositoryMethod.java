package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public interface RepositoryMethod {
    Call getDescription(URI baseUrl, Class<?> declaringClass, ImmutableMultimap<String, String> requestParams, Map<String, Object> pathVariables, Set<Object> unmappedArgs);
    boolean isRepositoryMethod(Method method);
}
