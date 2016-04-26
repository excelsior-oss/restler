package org.restler.spring.data.calls;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.spring.data.util.Repositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SaveSeveralCallEnhancer implements CallEnhancer {
    private final Repositories repositories;

    public SaveSeveralCallEnhancer(Repositories repositories) {
        this.repositories = repositories;
    }

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        if(call instanceof SaveSeveralCall) {
            Iterable<Object> objectsForSave = ((SaveSeveralCall) call).getObjectsForSave();

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

        return callExecutor.execute(call);
    }

    public static class SaveSeveralCall implements Call {

        private final Type returnType;
        private final Iterable<Object> objectsForSave;

        public SaveSeveralCall(Iterable<Object> objectsForSave, Type returnType) {
            this.objectsForSave = objectsForSave;
            this.returnType = returnType;
        }

        @Override
        public Type getReturnType() {
            return returnType;
        }

        @Override
        public Call withReturnType(Type type) {
            return new SaveSeveralCall(objectsForSave, type);
        }

        public Iterable<Object> getObjectsForSave() {
            return objectsForSave;
        }
    }
}
