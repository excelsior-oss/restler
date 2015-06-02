package org.restler.http;

import org.restler.client.ServiceMethodDescription;
import org.restler.client.ServiceMethodExecutor;
import org.restler.client.ServiceMethodInvocation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class HttpServiceMethodExecutor implements ServiceMethodExecutor {

    private final HttpRequestExecutor requestExecutor;

    public HttpServiceMethodExecutor(HttpRequestExecutor requestExecutor){
        this.requestExecutor = requestExecutor;
    }

    @Override
    public <T> T execute(ServiceMethodInvocation<T> invocation) {

        ExecutableRequest<T> executableRequest = toRequest(invocation);
        ResponseEntity<T> responseEntity = requestExecutor.execute(executableRequest);
        return responseEntity.getBody();
    }

    private <T> ExecutableRequest<T> toRequest(ServiceMethodInvocation<T> invocation) {
        ServiceMethodDescription<T> method = invocation.getMethod();

        URI target = UriComponentsBuilder.fromHttpUrl(invocation.getBaseUrl()).
                path(method.getUriTemplate()).
                queryParams(invocation.getRequestParams()).
                buildAndExpand(invocation.getPathVariables()).toUri();

        return new ExecutableRequest<>(target, method.getHttpMethod(), invocation.getRequestBody(), method.getReturnType());
    }
}
