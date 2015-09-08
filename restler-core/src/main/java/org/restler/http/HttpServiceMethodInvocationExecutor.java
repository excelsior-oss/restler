package org.restler.http;

import org.restler.client.ServiceMethod;
import org.restler.client.ServiceMethodInvocation;
import org.restler.client.ServiceMethodInvocationExecutor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class HttpServiceMethodInvocationExecutor implements ServiceMethodInvocationExecutor {

    private final RequestExecutionChain executors;

    public HttpServiceMethodInvocationExecutor(RequestExecutionChain executors) {
        this.executors = executors;
    }

    @Override
    public <T> T execute(ServiceMethodInvocation<T> invocation) {

        Request<T> request = toRequest(invocation);
        Response<T> responseEntity = executors.execute(request);
        if (responseEntity instanceof SuccessfulResponse) {
            return ((SuccessfulResponse<T>) responseEntity).getResult();
        } else if (responseEntity instanceof FailedResponse) {
            FailedResponse<T> failedResponse = (FailedResponse<T>) responseEntity;
            throw new HttpExecutionException("Could not execute request", failedResponse.getCause(), failedResponse.getResponseBody().orElse(null));
        } else {
            throw new AssertionError("Should never happen");
        }
    }

    private <T> Request<T> toRequest(ServiceMethodInvocation<T> invocation) {
        ServiceMethod<T> method = invocation.getMethod();

        URI target = UriComponentsBuilder.fromUri(invocation.getBaseUrl()).
                path(method.getUriTemplate()).
                queryParams(invocation.getRequestParams()).
                buildAndExpand(invocation.getPathVariables()).toUri();

        return new Request<>(target, HttpMethod.valueOf(method.getHttpMethod().name()), invocation.getRequestBody(), method.getReturnType());
    }
}
