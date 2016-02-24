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
    private final HashMap<Class<?>, Repository> repositories = new HashMap<>();
    private final List<Class<?>> repositoriesList;
    private final ClientFactory clientFactory;

    public Repositories(List<Class<?>> repositories, ClientFactory factory) {
        this.repositoriesList = repositories;
        this.clientFactory = factory;
    }

    public void initialize() {
        for(Class<?> repositoryClass : repositoriesList) {
            this.repositories.put(repositoryClass, (Repository) clientFactory.produceClient(repositoryClass));
        }
    }

    public Repository getByResourceClass(Class<?> resourceClass) {
        Repository[] result = {null};
        repositories.forEach((clazz, repository)->{
            Type[] interfaces = clazz.getGenericInterfaces();

            Type[] genericTypes = ((ParameterizedTypeImpl)interfaces[0]).getActualTypeArguments();

            Class<?> genericId = TypeToken.of(genericTypes[0]).getRawType();

            if(genericId.equals(resourceClass)) {
                result[0] = repository;
            }
        });

        return result[0];
    }
}
