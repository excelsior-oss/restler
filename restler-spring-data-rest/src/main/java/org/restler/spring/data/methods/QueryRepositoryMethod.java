package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;

/**
 * Custom query method implementation.
 */
public class QueryRepositoryMethod extends DefaultRepositoryMethod {

    private static final Method[] crudMethods;

    static {
        crudMethods = PagingAndSortingRepository.class.getMethods();
    }

    private final Method method;

    public QueryRepositoryMethod(Method method) {
        this.method = method;
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        return !Arrays.stream(crudMethods).anyMatch(crudMethod -> crudMethod.equals(method));
    }

    @Override
    protected Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        return new HttpCall(uri, HttpMethod.GET, null, ImmutableMultimap.of("Content-Type", "application/json"), method.getGenericReturnType());
    }

    @Override
    protected String getPathPart(Object[] args) {
        RestResource methodAnnotation = method.getDeclaredAnnotation(RestResource.class);
        String methodName = method.getName();

        if (methodAnnotation != null && !methodAnnotation.path().isEmpty()) {
            methodName = methodAnnotation.path();
        }

        return "search/" + methodName;
    }
}
