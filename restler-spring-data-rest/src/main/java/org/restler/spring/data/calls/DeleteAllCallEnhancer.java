package org.restler.spring.data.calls;

import org.restler.client.Call;
import org.restler.client.CallExecutor;
import org.restler.client.RestlerException;
import org.restler.spring.data.proxy.ResourceProxy;
import org.restler.spring.data.util.Repositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.lang.reflect.Type;
import java.util.List;

public class DeleteAllCallEnhancer extends CustomCallEnhancer<DeleteAllCallEnhancer.DeleteAllCall> {
    private final Repositories repositories;

    public DeleteAllCallEnhancer(Repositories repositories) {
        super(DeleteAllCall.class);
        this.repositories = repositories;
    }

    @Override
    protected Object enhance(DeleteAllCall call, CallExecutor callExecutor) {
        Object list = callExecutor.execute(call.getCall());

        ((List<?>) list).stream().
                filter(item -> item instanceof ResourceProxy).
                forEach(item -> {
                    ResourceProxy resourceProxy = (ResourceProxy) item;
                    Repository repository = repositories.getByResourceClass(resourceProxy.getObject().getClass()).orElse(null);
                    if (repository == null || !(repository instanceof CrudRepository)) {
                        throw new RestlerException("Could not find repository for " + item);
                    }
                    ((CrudRepository) repository).delete(item);
                });

        return null;
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
