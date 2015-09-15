package org.restler.http.security.authentication;

import org.restler.client.HttpCall;
import org.restler.http.Header;

import java.util.List;

public abstract class HeaderBasedAuthenticationStrategy implements AuthenticationStrategy {

    protected abstract List<Header> headers(AuthenticationContext context);

    @Override
    public <T> HttpCall<T> authenticate(HttpCall<T> call, AuthenticationContext context) {
        HttpCall<T> res = call;
        for (Header header : headers(context)) {
            res = res.setHeader(header.name, header.values());
        }
        return res;
    }
}
