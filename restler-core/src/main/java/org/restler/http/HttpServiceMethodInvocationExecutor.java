package org.restler.http;

import org.restler.client.HttpCall;
import org.restler.client.HttpCallExecutor;

public class HttpServiceMethodInvocationExecutor implements HttpCallExecutor {

    private final RequestExecutionChain executors;

    public HttpServiceMethodInvocationExecutor(RequestExecutionChain executors) {
        this.executors = executors;
    }

    @Override
    public <T> T execute(HttpCall<T> call) {

        Response<T> responseEntity = executors.execute(call);
        if (responseEntity instanceof SuccessfulResponse) {
            return ((SuccessfulResponse<T>) responseEntity).getResult();
        } else if (responseEntity instanceof FailedResponse) {
            FailedResponse<T> failedResponse = (FailedResponse<T>) responseEntity;
            throw new HttpExecutionException("Could not execute request", failedResponse.getCause(), failedResponse.getResponseBody().orElse(null));
        } else {
            throw new AssertionError("Should never happen");
        }
    }

}
