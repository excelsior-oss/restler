package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Represent some repository method, it allows build call description for different methods.
 */
public interface RepositoryMethod {
    /**
     * Builds call description.
     * @param declaringClass class object whose method was called.
     * @param unmappedArgs arguments that wasn't mapped into requestParams and pathVariables.
     */
    Call getDescription(URI baseUrl, Class<?> declaringClass, ImmutableMultimap<String, String> requestParams, Map<String, Object> pathVariables, Set<Object> unmappedArgs);

    /**
     * Checks that method is supported by current implementation.
     */
    boolean isRepositoryMethod(Method method);
}
