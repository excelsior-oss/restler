package org.restler.spring.data.calls;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.spring.data.util.Repositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.lang.reflect.Type;
import java.util.List;

public class DeleteAllCallEnhancer implements CallEnhancer {
    private final Repositories repositories;

    public DeleteAllCallEnhancer(Repositories repositories) {
        this.repositories = repositories;
    }

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        if(call instanceof DeleteAllCall) {
            Object list = callExecutor.execute(((DeleteAllCall) call).getCall());

            if(list instanceof List) {
                List objects = (List)list;

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

        return callExecutor.execute(call);
    }

    public static class DeleteAllCall implements Call {

        private final Call call;
        private final Type returnType;

        public DeleteAllCall(Call call, Type returnType) {
            this.call = call;
            this.returnType = returnType;
        }

        @Override
        public Type getReturnType() {
            return returnType;
        }

        @Override
        public Call withReturnType(Type type) {
            return new DeleteAllCall(call, type);
        }

        public Call getCall() {
            return call;
        }
    }
}
