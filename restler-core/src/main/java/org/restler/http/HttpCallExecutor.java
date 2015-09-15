package org.restler.http;

import org.restler.client.Call;
import org.restler.client.CallExecutor;

public class HttpCallExecutor implements CallExecutor {

    private final RequestExecutor executors;

    public HttpCallExecutor(RequestExecutor executor) {
        this.executors = executor;
    }

    @Override
    public Object execute(Call call) {

        Response responseEntity = executors.execute(call);
        if (responseEntity instanceof SuccessfulResponse) {
            return ((SuccessfulResponse) responseEntity).getResult();
        } else if (responseEntity instanceof FailedResponse) {
            FailedResponse failedResponse = (FailedResponse) responseEntity;
            throw new HttpExecutionException("Could not execute request", failedResponse.getCause(), failedResponse.getResponseBody().orElse(""));
        } else {
            throw new AssertionError("Should never happen");
        }

    }

}
