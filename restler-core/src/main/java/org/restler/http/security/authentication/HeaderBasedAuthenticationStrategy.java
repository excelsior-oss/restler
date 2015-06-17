package org.restler.http.security.authentication;

import org.restler.http.Request;

public abstract class HeaderBasedAuthenticationStrategy implements AuthenticationStrategy {

    protected abstract String getHeaderName(AuthenticationContext context);

    protected abstract String getHeaderValue(AuthenticationContext context);

    @Override
    public <T> Request<T> authenticate(Request<T> request, AuthenticationContext context) {
        return request.setHeader(getHeaderName(context), getHeaderValue(context));
    }
}
