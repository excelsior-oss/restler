package org.restler.http.security.authentication;

import org.restler.http.HttpRequestExecutor;
import org.springframework.http.HttpHeaders;

/**
 * Created by oleg on 15.06.2015.
 */
public class HTTPBasicAuthenticationRequestExecutor extends HeaderAuthenticationRequestExecutor {

    public HTTPBasicAuthenticationRequestExecutor(HttpRequestExecutor executor, AuthenticationContext context) {
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
