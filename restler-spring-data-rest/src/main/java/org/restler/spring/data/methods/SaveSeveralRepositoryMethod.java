package org.restler.spring.data.methods;

import org.restler.client.Call;
import org.restler.client.RestlerException;
import org.restler.spring.data.chain.ChainCall;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.spring.data.util.ArrayListType;
import org.restler.spring.data.util.Repositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SaveSeveralRepositoryMethod extends DefaultRepositoryMethod {

    private final Repositories repositories;

    public SaveSeveralRepositoryMethod(Repositories repositories) {
        this.repositories = repositories;
    }

    @Override
    protected Call getCall(URI uri, Class<?> declaringClass, Object[] args) {
        Type itemType = getRepositoryType(declaringClass).getActualTypeArguments()[0];

        List<Call> calls = new ArrayList<>();

        calls.add(null);

        return new ChainCall((Object o)->save((Iterable<Object>)args[0]), calls, new ArrayListType(itemType));
    }

    @Override
    protected String getPathPart(Object[] args) {
        return "";
    }

    @Override
    public boolean isRepositoryMethod(Method method) {
        try {
            return CrudRepository.class.getMethod("save", Iterable.class).equals(method);
        } catch (NoSuchMethodException e) {
            throw new RestlerException("Can't find CrudRepository.findAll method.", e);
        }
    }

    private Object save(Iterable<Object> objectsForSave) {
        List<Object> result = new ArrayList<>();
        for(Object objectForSave : objectsForSave) {
            Repository repository;

            if(objectForSave instanceof ResourceProxy) {
                repository = repositories.getByResourceClass(((ResourceProxy)objectForSave).getObject().getClass()).orElse(null);
            } else {
                repository = repositories.getByResourceClass(objectForSave.getClass()).orElse(null);
            }

            if(repository != null && repository instanceof CrudRepository) {
                result.add(((CrudRepository)repository).save(objectForSave));
            }
        }

        return result;
    }
}
