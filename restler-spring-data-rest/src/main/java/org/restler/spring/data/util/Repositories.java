package org.restler.spring.data.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import org.restler.client.CoreModule;
import org.restler.client.RestlerException;
import org.springframework.data.repository.Repository;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class Repositories {

    private final List<Class<?>> repositoriesList;
    private final LoadingCache<Class<?>, Repository> cache;

    public Repositories(List<Class<?>> repositories, CoreModule coreModule) {
        this.repositoriesList = repositories;
        this.cache = CacheBuilder.newBuilder().
                build(new CacheLoader<Class<?>, Repository>() {
                    @Override
                    public Repository load(Class<?> aClass) throws Exception {
                        return (Repository) coreModule.produceClient(aClass);
                    }
                });
    }

    public Optional<Repository> getByResourceClass(Class<?> resourceClass) {

        Class<?> resultClass = repositoriesList.stream().filter((Class<?> clazz)->{
            Type[] interfaces = clazz.getGenericInterfaces();
            Type[] genericTypes = ((ParameterizedTypeImpl)interfaces[0]).getActualTypeArguments();
            Class<?> genericId = TypeToken.of(genericTypes[0]).getRawType();

            return genericId.equals(resourceClass);
        }).findFirst().get();

        if(resultClass != null) {
            try {
                return Optional.of(cache.get(resultClass));
            } catch (ExecutionException e) {
                throw new RestlerException("Can't get repository by class " + resultClass, e);
            }
        }

        return Optional.empty();
    }
}
