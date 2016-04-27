package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.calls.ChainCall;
import org.restler.spring.data.util.ArrayListType;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FindAllByIdRepositoryMethod extends DefaultRepositoryMethod {

    private static final Method findAllMethod;

    static {
        try {
            findAllMethod = CrudRepository.class.getMethod("findAll", Iterable.class);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.findAll method.", e);
        }
    }

    @Override
    protected Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        Type itemType = getRepositoryType(declaringClass).getActualTypeArguments()[0];

        Iterable ids = (Iterable) args[0];

        List<Call> calls = new ArrayList<>();

        for(Object id : ids) {
            calls.add(new HttpCall( new UriBuilder(uri.toString() + "/" + id).build(), HttpMethod.GET, null, ImmutableMultimap.of("Content-Type", "application/json"), itemType));
        }

        return new ChainCall((prevResult, object)->addToList((List<Object>)prevResult, object), calls, new ArrayListType(itemType));
    }

    @Override
    protected String getPathPart(Object[] args) {
        return "";
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        return findAllMethod.equals(method);
    }

    private Object addToList(List<Object> list, Object item) {
        if(list == null) {
            list = new ArrayList<Object>();
        }

        list.add(item);
        return list;
    }
}
