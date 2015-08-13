package org.restler.http.security.authentication;

import org.restler.http.Request;

import java.util.List;

public abstract class HeaderBasedAuthenticationStrategy implements AuthenticationStrategy {

    protected abstract List<Header> headers(AuthenticationContext context);

    @Override
    public <T> Request<T> authenticate(Request<T> request, AuthenticationContext context) {
        Request<T> res = request;
        for (Header header : headers(context)) {
            res = res.setHeader(header.name, header.values());
        }
        return res;
    }
}
