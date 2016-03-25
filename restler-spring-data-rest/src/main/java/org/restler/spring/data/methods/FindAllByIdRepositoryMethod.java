package org.restler.spring.data.methods;

import com.google.common.collect.ImmutableMultimap;
import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.http.HttpCall;
import org.restler.http.HttpMethod;
import org.restler.spring.data.chain.ChainCall;
import org.restler.util.UriBuilder;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FindAllByIdRepositoryMethod extends DefaultRepositoryMethod {

    @Override
    protected Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        Type itemType = getRepositoryType(declaringClass).getActualTypeArguments()[0];

        Iterable ids = (Iterable) args[0];


        List<Call> calls = new ArrayList<>();

        for(Object id : ids) {
            calls.add(new HttpCall( new UriBuilder(uri.toString() + "/" + id).build(), HttpMethod.GET, null, ImmutableMultimap.of("Content-Type", "application/json"), itemType));
        }

        List<Object> list = new ArrayList<>();
        return new ChainCall((Object o)->addToList(o, list), calls, new ArrayListType(itemType));
    }

    @Override
    protected String getPathPart(Object[] args) {
        return "";
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        try {
            return CrudRepository.class.getMethod("findAll", Iterable.class).equals(method);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.findAll method.", e);
        }
    }

    private Object addToList(Object item, List<Object> list) {
        list.add(item);
        return list;
    }

    private class ArrayListType implements ParameterizedType {

        private final Type itemType;

        public ArrayListType(Type itemType) {
            this.itemType = itemType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] {itemType};
        }

        @Override
        public Type getRawType() {
            return ArrayList.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
