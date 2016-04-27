package org.restler.spring.data.methods;

import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.spring.data.calls.SaveSeveralCallEnhancer;
import org.restler.spring.data.util.ArrayListType;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;

public class SaveSeveralRepositoryMethod extends DefaultRepositoryMethod {

    private static final Method saveMethod;

    static {
        try {
            saveMethod = CrudRepository.class.getMethod("save", Iterable.class);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.findAll method.", e);
        }
    }

    @Override
    protected Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        Type itemType = getRepositoryType(declaringClass).getActualTypeArguments()[0];

        return new SaveSeveralCallEnhancer.SaveSeveralCall((Iterable<Object>)args[0], new ArrayListType(itemType));
    }

    @Override
    protected String getPathPart(Object[] args) {
        return "";
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        return saveMethod.equals(method);
    }

}
