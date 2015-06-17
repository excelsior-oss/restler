package org.restler.http.security.authentication;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

/**
 * The implementation that authenticates a request adding a cookie.
 */
public class CookieAuthenticationStrategy extends HeaderBasedAuthenticationStrategy {

    protected static final String JSESSIONID = "JSESSIONID";

    private final String cookieName;

    /**
     * Creates the strategy that uses JSESSIONID cookie.
     */
    public CookieAuthenticationStrategy() {
        this(JSESSIONID);
    }

    /**
     * Creates the strategy that uses a custom cookie.
     *
     * @param cookieName the name of the cookie.
     */
    public CookieAuthenticationStrategy(String cookieName) {
        if (StringUtils.isEmpty(cookieName))
            throw new IllegalArgumentException("Authentication cookie name must be not empty.");
        this.cookieName = cookieName;
    }

    @Override
    protected String getHeaderName(AuthenticationContext context) {
        return HttpHeaders.COOKIE;
    }

    @Override
    protected String getHeaderValue(AuthenticationContext context) {
        Object token = context.getAuthenticationToken();
        String cookieValue = token == null ? null : token.toString();
        return cookieName + "=" + cookieValue + ";";
    }
}
