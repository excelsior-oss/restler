package org.restler.http;

import org.restler.client.Call;
import org.restler.client.CallExecutor;

public class HttpCallExecutor implements CallExecutor {

    private final RequestExecutor executor;

    public HttpCallExecutor(RequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Object execute(Call call) {

        Response responseEntity = executor.execute(call);
        if (responseEntity instanceof SuccessfulResponse) {
            return ((SuccessfulResponse) responseEntity).getResult();
        } else if (responseEntity instanceof FailedResponse) {
            FailedResponse failedResponse = (FailedResponse) responseEntity;
            throw new HttpExecutionException("Could not execute request", failedResponse.getCause(), failedResponse.getStatus(), failedResponse.getResponseBody().orElse(""));
        } else {
            throw new AssertionError("Should never happen");
        }

    }

}
