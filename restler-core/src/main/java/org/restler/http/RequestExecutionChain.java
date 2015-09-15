package org.restler.http;

import org.restler.client.HttpCall;

import java.util.Arrays;
import java.util.List;

public class RequestExecutionChain implements RequestExecutor {

    private final RequestExecutor requestExecutor;
    private final RequestExecutionAdvice[] advices;
    private final int index;

    public RequestExecutionChain(RequestExecutor requestExecutor, List<RequestExecutionAdvice> advices) {
        this(requestExecutor, advices.toArray(new RequestExecutionAdvice[advices.size()]), 0);
    }

    private RequestExecutionChain(RequestExecutor requestExecutor, RequestExecutionAdvice[] advices, int index) {

        if (Arrays.asList(advices).contains(null))
            throw new NullPointerException("Null advice is not allowed in the chain.");

        this.requestExecutor = requestExecutor;
        this.advices = advices;
        this.index = index;
    }

    public <T> Response<T> execute(HttpCall<T> call) {
        if (index >= advices.length) {
            return requestExecutor.execute(call);
        } else {
            return advices[index].advice(call, tail());
        }
    }

    private RequestExecutionChain tail() {
        return new RequestExecutionChain(requestExecutor, advices, index + 1);
    }

}
