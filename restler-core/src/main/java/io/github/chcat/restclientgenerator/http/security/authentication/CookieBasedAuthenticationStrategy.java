package io.github.chcat.restclientgenerator.http.security.authentication;

import io.github.chcat.restclientgenerator.http.RequestExecutor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

/**
 * Created by pasa on 19.02.2015.
 */
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
