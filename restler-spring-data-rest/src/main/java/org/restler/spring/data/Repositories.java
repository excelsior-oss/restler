package org.restler.spring.data;

import com.google.common.reflect.TypeToken;
import org.restler.client.ClientFactory;
import org.restler.client.RestlerException;
import org.springframework.data.repository.Repository;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class Repositories {
    private HashMap<Class<?>, Repository> repositories = new HashMap<>();
    private List<Class<?>> repositoriesList;
    private ClientFactory clientFactory;

    public Repositories(List<Class<?>> repositories, ClientFactory factory) {
        this.repositoriesList = repositories;
        this.clientFactory = factory;
    }

    public void initialize() {
        for(Class<?> repositoryClass : repositoriesList) {
            this.repositories.put(repositoryClass, (Repository) clientFactory.produceClient(repositoryClass));
        }
    }

    public Repository getByClass(Class<?> repositoryClass) {
        Repository repository = repositories.get(repositoryClass);
        if(repository == null) {
            throw new RestlerException("Can't find repository.");
        }
        return repository;
    }

    public Repository getByIdClass(Class<?> idClass) {
        Repository[] result = {null};
        repositories.forEach((clazz, repository)->{
            Type[] interfaces = clazz.getGenericInterfaces();

            Type[] genericTypes = ((ParameterizedTypeImpl)interfaces[0]).getActualTypeArguments();

            Class<?> genericId = TypeToken.of(genericTypes[1]).getRawType();

            if(genericId == idClass) {
                result[0] = repository;
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
                result[0] = repository;
            }
        });

        return result[0];
    }
}
