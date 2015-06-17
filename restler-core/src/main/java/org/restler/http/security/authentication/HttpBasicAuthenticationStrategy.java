package org.restler.http.security.authentication;

import org.springframework.http.HttpHeaders;

public class HttpBasicAuthenticationStrategy extends HeaderBasedAuthenticationStrategy {

    @Override
    protected String getHeaderName(AuthenticationContext context) {
        return HttpHeaders.AUTHORIZATION;
    }

    @Override
    protected String getHeaderValue(AuthenticationContext context) {
        Object token = context.getAuthenticationToken();
        String tokenValue = token == null ? null : token.toString();
        return "Basic " + tokenValue;
    }
}
