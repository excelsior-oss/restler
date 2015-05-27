package org.restler.http.security.authentication;

import org.restler.http.RequestExecutor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

public class CookieBasedAuthenticationStrategy implements AuthenticationStrategy {

    private static final String COOKIE_HEADER = "Cookie";

    protected String cookieName;
    protected String cookieValue;

    public CookieBasedAuthenticationStrategy() { this("JSESSIONID"); }

    public CookieBasedAuthenticationStrategy(String cookieName){
        this.cookieName = cookieName;
    }

    public void setAuthenticationToken(Object token){
        cookieValue = token == null ? null : token.toString();
    }

    public <T> ResponseEntity<T> executeAuthenticatedRequest(RequestExecutor executor, RequestEntity<?> request, Class<T> responseType) {
        request.getHeaders().add(COOKIE_HEADER, cookieName + "=" + cookieValue);
        return executor.execute(request,responseType);
    }
}
