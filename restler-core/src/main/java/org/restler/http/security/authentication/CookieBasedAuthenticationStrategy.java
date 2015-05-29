package org.restler.http.security.authentication;

import org.restler.http.ExecutableRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

/**
 * The implementation that authenticates a request adding a cookie.
 */
public class CookieBasedAuthenticationStrategy implements AuthenticationStrategy {

    protected static final String JSESSIONID = "JSESSIONID";

    protected String cookieName;

    /**
     * Creates the strategy that uses JSESSIONID cookie.
     */
    public CookieBasedAuthenticationStrategy() { this(JSESSIONID); }

    /**
     * Creates the strategy that uses a custom cookie.
     * @param cookieName the name of the cookie.
     */
    public CookieBasedAuthenticationStrategy(String cookieName){
        if (StringUtils.isEmpty(cookieName)) throw new IllegalArgumentException("Authentication cookie name must be not empty.");
        this.cookieName = cookieName;
    }

    public <T> ResponseEntity<T> authenticatedAndExecute(ExecutableRequest<T> request, AuthenticationContext context) {
        Object token = context.getAuthenticationToken();
        String cookieValue = token == null ? null : token.toString();
        if (!StringUtils.isEmpty(cookieValue)) {
            request.addHeader(HttpHeaders.COOKIE, cookieName + "=" + cookieValue +";");
        }
        return request.execute();
    }
}
