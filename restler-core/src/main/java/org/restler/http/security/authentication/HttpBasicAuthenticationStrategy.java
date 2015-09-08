package org.restler.http.security.authentication;

import com.google.common.net.HttpHeaders;
import org.restler.http.Header;

import java.util.Collections;
import java.util.List;

public class HttpBasicAuthenticationStrategy extends HeaderBasedAuthenticationStrategy {

    @Override
    protected List<Header> headers(AuthenticationContext context) {
        return Collections.singletonList(new Header(HttpHeaders.AUTHORIZATION, value(context)));
    }

    private String value(AuthenticationContext context) {
        Object token = context.getAuthenticationToken();
        String tokenValue = token == null ? null : token.toString();
        return "Basic " + tokenValue;
    }
}
