package org.restler.http.security.authentication;

import org.restler.client.Call;
import org.restler.http.Header;
import org.restler.http.HttpCall;

import java.util.List;

public abstract class HeaderBasedAuthenticationStrategy implements AuthenticationStrategy {

    protected abstract List<Header> headers(AuthenticationContext context);

    @Override
    public Call authenticate(Call call, AuthenticationContext context) {
        HttpCall res = (HttpCall) call;
        for (Header header : headers(context)) {
            res = res.setHeader(header.name, header.values());
        }
        return res;
    }
}
