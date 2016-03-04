package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Represents some repository method, allowing building of call descriptions for different methods.
 */
public interface RepositoryMethod {
    /**
     * Builds call description.
     * @param declaringClass class object whose method was called.
     * @param unmappedArgs arguments that weren't mapped into {@code requestParams} and {@code pathVariables}.
     */
    Call getDescription(URI baseUrl, Class<?> declaringClass, ImmutableMultimap<String, String> requestParams, Map<String, Object> pathVariables, Set<Object> unmappedArgs);

    /**
     * Checks that method is supported by current implementation.
     */
    boolean isRepositoryMethod(Method method);
}
