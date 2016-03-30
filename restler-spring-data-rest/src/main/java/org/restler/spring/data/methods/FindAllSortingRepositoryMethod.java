package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.util.ArrayListType;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;

public class FindAllSortingRepositoryMethod extends DefaultRepositoryMethod {
    @Override
    protected Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        Type itemType = getRepositoryType(declaringClass).getActualTypeArguments()[0];
        return new HttpCall(uri, HttpMethod.GET, null, ImmutableMultimap.of("Content-Type", "application/json"), new ArrayListType(itemType));
    }

    @Override
    protected String getPathPart(Object[] args) {
        Sort sort = (Sort)args[0];
        return "?sort=" + sort.toString().replace(": ", ",");
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        try {
            return PagingAndSortingRepository.class.getMethod("findAll", Sort.class).equals(method);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.findAll method.", e);
        }
    }
}
