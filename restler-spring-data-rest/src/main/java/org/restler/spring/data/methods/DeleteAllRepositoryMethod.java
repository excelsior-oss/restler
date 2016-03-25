package org.restler.spring.data.methods;

import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.spring.data.chain.ChainCall;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.spring.data.util.Repositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DeleteAllRepositoryMethod extends DefaultRepositoryMethod {

    private final Repositories repositories;

    public DeleteAllRepositoryMethod(Repositories repositories) {
        this.repositories = repositories;
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        try {
            return CrudRepository.class.getMethod("deleteAll").equals(method);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.deleteAll method.", e);
        }
    }

    @Override
    public Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        List<Call> calls = new ArrayList<>();

        Call getAllCall = new FindAllRepositoryMethod().getCall(uri, declaringClass, args);
        calls.add(getAllCall);

        return new ChainCall((Object o)->deleteAll(o, repositories), calls, void.class);
    }

    @Override
    public String getPathPart(Object[] args) {
        return "";
    }

    private Object deleteAll(Object object, Repositories repositories) {
        if(object instanceof List) {
            List objects = (List)object;

            for(Object item : objects) {
                if(item instanceof ResourceProxy) {
                    ResourceProxy resourceProxy = (ResourceProxy)item;
                    Repository repository = repositories.getByResourceClass(resourceProxy.getObject().getClass()).orElse(null);
                    if(repository != null && repository instanceof CrudRepository) {
                        ((CrudRepository)repository).delete(item);
                    }
                }
            }
        }

        return null;
    }
}
