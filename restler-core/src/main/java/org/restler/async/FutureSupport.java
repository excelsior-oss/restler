package org.restler.async;

import org.restler.client.AbstractWrapperHandler;
import org.restler.client.Call;
import org.restler.client.CallExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class FutureSupport extends AbstractWrapperHandler<Future<?>> {

    private final Executor threadPool;

    public FutureSupport(Executor threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    protected Class<?> wrapperClass() {
        return Future.class;
    }

    @Override
    protected Future<?> execute(CallExecutor callExecutor, Call actualCall) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        threadPool.execute(() -> future.complete(callExecutor.execute(actualCall)));
        return future;
    }

}
