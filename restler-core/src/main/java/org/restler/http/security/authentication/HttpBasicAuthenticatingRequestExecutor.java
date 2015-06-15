package org.restler.http.security.authentication;

import org.restler.http.HttpRequestExecutor;
import org.springframework.http.HttpHeaders;

public class HttpBasicAuthenticatingRequestExecutor extends HeaderAuthenticatingRequestExecutor {

    public HttpBasicAuthenticatingRequestExecutor(HttpRequestExecutor executor, AuthenticationContext context) {
        super(executor, context);
    }

    @Override
    protected String getHeaderName() {
        return HttpHeaders.AUTHORIZATION;
    }

    @Override
    protected String getHeaderValue() {
        Object token = context.getAuthenticationToken();
        String tokenValue = token == null ? null : token.toString();
        return "Basic " + tokenValue;
    }
}
