package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.calls.ConvertToPageCallEnhancer;
import org.restler.spring.data.util.ArrayListType;
import org.restler.spring.data.util.PageType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;

public class FindAllPageableRepositoryMethod extends DefaultRepositoryMethod {

    private static final Method findAllMethod;

    static {
        try {
            findAllMethod = PagingAndSortingRepository.class.getMethod("findAll", Pageable.class);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.findAll method.", e);
        }
    }

    @Override
    protected Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        Type itemType = getRepositoryType(declaringClass).getActualTypeArguments()[0];
        Call httpCall = new HttpCall(uri, HttpMethod.GET, null, ImmutableMultimap.of("Content-Type", "application/json"), new ArrayListType(itemType));
        return new ConvertToPageCallEnhancer.ConvertToPageCall(httpCall, new PageType(itemType));
    }

    @Override
    protected String getPathPart(Object[] args) {
        Pageable page = (Pageable) args[0];
        return "?page=" + page.getPageNumber() + "&size=" + page.getPageSize() + "&sort=" + page.getSort().toString().replace(": ", ",");
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        return findAllMethod.equals(method);
    }
}
