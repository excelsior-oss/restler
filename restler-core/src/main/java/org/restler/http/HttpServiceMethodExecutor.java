package org.restler.http;

import org.restler.client.ServiceMethod;
import org.restler.client.ServiceMethodInvocationExecutor;
import org.restler.client.ServiceMethodInvocation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class HttpServiceMethodExecutor implements ServiceMethodInvocationExecutor {

    private final Executor requestExecutor;

    public HttpServiceMethodExecutor(Executor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    @Override
    public <T> T execute(ServiceMethodInvocation<T> invocation) {

        Request<T> executableRequest = toRequest(invocation);
        ResponseEntity<T> responseEntity = requestExecutor.execute(executableRequest);
        return responseEntity.getBody();
    }

    private <T> Request<T> toRequest(ServiceMethodInvocation<T> invocation) {
        ServiceMethod<T> method = invocation.getMethod();

        URI target = UriComponentsBuilder.fromHttpUrl(invocation.getBaseUrl()).
                path(method.getUriTemplate()).
                queryParams(invocation.getRequestParams()).
                buildAndExpand(invocation.getPathVariables()).toUri();

        return new Request<>(target, method.getHttpMethod(), invocation.getRequestBody(), method.getReturnType());
    }
}
