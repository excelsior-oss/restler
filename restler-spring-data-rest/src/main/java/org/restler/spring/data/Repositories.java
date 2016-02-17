package org.restler.spring.data;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import org.restler.client.ClientFactory;
import org.restler.client.RestlerException;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import sun.java2d.pipe.SpanShapeRenderer;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

public class Repositories {
    private HashMap<Class<?>, AbstractMap.SimpleEntry<Repository, String>> repositories = new HashMap<>();
    private String baseUri;
    private List<Class<?>> repositoriesList;
    private ClientFactory clientFactory;

    public Repositories(String baseUri, List<Class<?>> repositories, ClientFactory factory) {
        this.baseUri = baseUri;
        this.repositoriesList = repositories;
        this.clientFactory = factory;
    }

    public void initialize() {
        for(Class<?> repositoryClass : repositoriesList) {
            String repositoryUri = baseUri + "/" + getRepositoryPath(repositoryClass);
            this.repositories.put(repositoryClass, new AbstractMap.SimpleEntry<>((Repository) clientFactory.produceClient(repositoryClass), repositoryUri));
        }
    }

    public Repository getByClass(Class<?> repositoryClass) {
        AbstractMap.SimpleEntry<Repository, String> repository = repositories.get(repositoryClass);
        if(repository == null) {
            throw new RestlerException("Can't find repository.");
        }
        return repository.getKey();
    }

    public Repository getByIdClass(Class<?> idClass) {
        Repository[] result = {null};
        repositories.forEach((clazz, repository)->{
            Type[] interfaces = clazz.getGenericInterfaces();

            Type[] genericTypes = ((ParameterizedTypeImpl)interfaces[0]).getActualTypeArguments();

            Class<?> genericId = TypeToken.of(genericTypes[1]).getRawType();

            if(genericId == idClass) {
                result[0] = repository.getKey();
            }
        });

        return result[0];
    }

    public Repository getByResourceClass(Class<?> resourceClass) {
        Repository[] result = {null};
        repositories.forEach((clazz, repository)->{
            Type[] interfaces = clazz.getGenericInterfaces();

            Type[] genericTypes = ((ParameterizedTypeImpl)interfaces[0]).getActualTypeArguments();

            Class<?> genericId = TypeToken.of(genericTypes[0]).getRawType();

            if(genericId == resourceClass) {
                result[0] = repository.getKey();
            }
        });

        return result[0];
    }

    public String getRepositoryUri(Class<?> repositoryClass) {
        AbstractMap.SimpleEntry<Repository, String> result = repositories.get(repositoryClass);

        if(result == null) {
            throw new RestlerException("Can't find repository uri");
        }

        return result.getValue();
    }

    private String getRepositoryPath(Class<?> repositoryClass) {
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
