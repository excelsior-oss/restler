package org.restler.spring.data.methods;

import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.spring.data.calls.DeleteAllCallEnhancer;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Method;
import java.net.URI;

public class DeleteAllRepositoryMethod extends DefaultRepositoryMethod {

    private static final Method deleteAllMethod;

    static {
        try {
            deleteAllMethod = CrudRepository.class.getMethod("deleteAll");
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.deleteAll method.", e);
        }
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        return deleteAllMethod.equals(method);
    }

    @Override
    public Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        Call getAllCall = new FindAllRepositoryMethod().getCall(uri, declaringClass, args);

        return new DeleteAllCallEnhancer.DeleteAllCall(getAllCall, void.class);
    }

    @Override
    public String getPathPart(Object[] args) {
        return "";
    }
}
