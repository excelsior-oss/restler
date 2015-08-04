package org.restler.http;

import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

public class ExecutionChain implements Executor {

    private final Executor executor;
    private final ExecutionAdvice[] advices;
    private final int index;

    public ExecutionChain(Executor executor, List<ExecutionAdvice> advices) {
        this(executor, advices.toArray(new ExecutionAdvice[advices.size()]), 0);
    }

    private ExecutionChain(Executor executor, ExecutionAdvice[] advices, int index) {

        if (Arrays.asList(advices).contains(null))
            throw new NullPointerException("Null advice is not allowed in the chain.");

        this.executor = executor;
        this.advices = advices;
        this.index = index;
    }

    public <T> ResponseEntity<T> execute(Request<T> request) {
        if (index >= advices.length) {
            return executor.execute(request);
        } else {
            return advices[index].advice(request, tail());
        }
    }

    private ExecutionChain tail() {
        return new ExecutionChain(executor, advices, index + 1);
    }

}
