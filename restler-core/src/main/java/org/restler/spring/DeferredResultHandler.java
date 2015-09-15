package org.restler.spring;

import org.restler.client.AbstractWrapperHandler;
import org.restler.client.Call;
import org.restler.client.CallExecutor;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.Executor;

public class DeferredResultHandler extends AbstractWrapperHandler<DeferredResult<Object>> {

    private final Executor threadPool;

    public DeferredResultHandler(Executor threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    protected Class<?> wrapperClass() {
        return DeferredResult.class;
    }

    @Override
    protected DeferredResult<Object> execute(CallExecutor callExecutor, Call actualCall) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        threadPool.execute(() -> deferredResult.setResult(callExecutor.execute(actualCall)));
        return deferredResult;
    }
}
