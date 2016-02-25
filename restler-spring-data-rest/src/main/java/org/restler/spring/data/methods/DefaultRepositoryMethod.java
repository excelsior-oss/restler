package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.reflect.TypeToken;
import org.restler.client.Call;
import org.restler.spring.data.RepositoryUtils;
import org.restler.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.net.URI;
import java.util.Map;
import java.util.Set;

public abstract class DefaultRepositoryMethod implements RepositoryMethod {
    public Call getDescription(URI baseUrl, Class<?> declaringClass, ImmutableMultimap<String, String> requestParams, Map<String, Object> pathVariables, Set<Object> unmappedArgs) {
        String methodMappedUriString;

        Class<?> idClass = TypeToken.of(getRepositoryType(declaringClass).getActualTypeArguments()[1]).getRawType();

        String repositoryUri = RepositoryUtils.getRepositoryPath(declaringClass.getInterfaces()[0]);

        methodMappedUriString = getPathPart(unmappedArgs.toArray());

        String uriTemplate = UriComponentsBuilder.fromUriString("/").pathSegment(repositoryUri, methodMappedUriString).build().toUriString();

        // TODO: implement more generic solution
        unmappedArgs.stream().
                filter(unmappedArg -> idClass.isAssignableFrom(unmappedArg.getClass())).
                forEach(unmappedArg -> pathVariables.put("id", unmappedArg));

        return getCall(url(baseUrl, uriTemplate, requestParams, pathVariables), declaringClass, unmappedArgs.toArray());
    }

    protected ParameterizedTypeImpl getRepositoryType(Class<?> repositoryClass) {
        Class repositoryType = (Class) repositoryClass.getMethods()[0].getDeclaringClass().getGenericInterfaces()[0];
        return (ParameterizedTypeImpl) repositoryType.getGenericInterfaces()[0];
    }

    protected abstract Call getCall(URI uri, Class<?> declaringClass, Object[] args);
    protected abstract String getPathPart(Object[] args);

    private URI url(URI baseUrl, String pathTemplate, ImmutableMultimap<String, String> queryParams, Map<String, Object> pathVariables) {
        return new UriBuilder(baseUrl).
                path(pathTemplate).
                queryParams(queryParams).
                pathVariables(pathVariables).build();
    }
}
