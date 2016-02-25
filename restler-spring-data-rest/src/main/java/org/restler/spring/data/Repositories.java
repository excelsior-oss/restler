package org.restler.spring.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import org.restler.client.ClientFactory;
import org.restler.client.RestlerException;
import org.springframework.data.repository.Repository;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Repositories {
    private final List<Class<?>> repositoriesList;
    private final ClientFactory clientFactory;
    private final LoadingCache<Class<?>, Repository> cache;

    public Repositories(List<Class<?>> repositories, ClientFactory factory) {
        this.repositoriesList = repositories;
        this.clientFactory = factory;
        this.cache = CacheBuilder.newBuilder().
                build(new CacheLoader<Class<?>, Repository>() {
                    @Override
                    public Repository load(Class<?> aClass) throws Exception {
                        return (Repository) clientFactory.produceClient(aClass);
                    }
                });
    }

    public Repository getByResourceClass(Class<?> resourceClass) {
        Repository[] result = {null};
        repositoriesList.forEach((clazz)->{
            Type[] interfaces = clazz.getGenericInterfaces();

            Type[] genericTypes = ((ParameterizedTypeImpl)interfaces[0]).getActualTypeArguments();

            Class<?> genericId = TypeToken.of(genericTypes[0]).getRawType();

            if(genericId.equals(resourceClass)) {
                try {
                    result[0] = cache.get(clazz);
                } catch (ExecutionException e) {
                    throw new RestlerException("Can't get repository by class " + clazz, e);
                }
            }
        });

        return result[0];
    }
}
