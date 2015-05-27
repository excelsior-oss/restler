package org.restler.http.security.authentication;

import org.restler.http.ExecutableRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class CookieBasedAuthenticationStrategy implements AuthenticationStrategy {

    protected String cookieName;
    protected String cookieValue;

    public CookieBasedAuthenticationStrategy() { this("JSESSIONID"); }

    public CookieBasedAuthenticationStrategy(String cookieName){
        this.cookieName = cookieName;
    }

    public void setAuthenticationToken(Object token){
        cookieValue = token == null ? null : token.toString();
    }

    public <T> ResponseEntity<T> executeAuthenticatedRequest(ExecutableRequest<T> request) {
        if (cookieValue != null) {
            request.addHeader(HttpHeaders.COOKIE, cookieName + "=" + cookieValue +";");
        }
        return request.execute();
    }
}
