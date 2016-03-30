package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.chain.ChainCall;
import org.restler.spring.data.util.ArrayListType;
import org.restler.spring.data.util.PageType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FindAllPageableRepositoryMethod extends DefaultRepositoryMethod {
    @Override
    protected Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        Type itemType = getRepositoryType(declaringClass).getActualTypeArguments()[0];
        Call httpCall = new HttpCall(uri, HttpMethod.GET, null, ImmutableMultimap.of("Content-Type", "application/json"), new ArrayListType(itemType));

        List<Call> calls = new ArrayList<>();
        calls.add(httpCall);

        return new ChainCall(this::convertToPage, calls, new PageType(itemType));
    }

    @Override
    protected String getPathPart(Object[] args) {
        Pageable page = (Pageable) args[0];
        return "?page=" + page.getPageNumber() + "&size=" + page.getPageSize() + "&sort=" + page.getSort().toString().replace(": ", ",");
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        try {
            return PagingAndSortingRepository.class.getMethod("findAll", Pageable.class).equals(method);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.findAll method.", e);
        }
    }

    private Object convertToPage(Object object) {
        return new PageImpl<>((List<?>)object);
    }
}
