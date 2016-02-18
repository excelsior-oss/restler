package org.restler.spring.data;

import org.restler.client.RestlerException;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RepositoryUtils {
    public static String getRepositoryPath(Class<?> repositoryClass) {
        RepositoryRestResource repositoryAnnotation = repositoryClass.getDeclaredAnnotation(RepositoryRestResource.class);

        String repositoryUriString;
        if (repositoryAnnotation == null || repositoryAnnotation.path().isEmpty()) {
            Type entityType = ((ParameterizedType) repositoryClass.getGenericInterfaces()[0]).getActualTypeArguments()[0];
            try {
                repositoryUriString = Class.forName(entityType.getTypeName()).getSimpleName().toLowerCase() + "s";
            } catch (ClassNotFoundException e) {
                throw new RestlerException("Could not find class for repository's entity type", e);
            }
        } else {
            repositoryUriString = repositoryAnnotation.path();
        }

        return repositoryUriString;
    }
}
