package org.restler.http.security.authentication;

import org.restler.http.HttpRequestExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

/**
 * The implementation that authenticates a request adding a cookie.
 */
public class CookieAuthenticatingRequestExecutor extends HeaderAuthenticationRequestExecutor {

    protected static final String JSESSIONID = "JSESSIONID";

    private final String cookieName;

    /**
     * Creates the strategy that uses JSESSIONID cookie.
     *
     * @param executor
     * @param context
     */
    public CookieAuthenticatingRequestExecutor(HttpRequestExecutor executor, AuthenticationContext context) {
        super(executor, context);
        cookieName = JSESSIONID;
    }

    /**
     * Creates the strategy that uses a custom cookie.
     *
     * @param cookieName the name of the cookie.
     * @param context
     * @param executor
     */
    public CookieAuthenticatingRequestExecutor(String cookieName, AuthenticationContext context, HttpRequestExecutor executor) {
        super(executor, context);
        if (StringUtils.isEmpty(cookieName))
            throw new IllegalArgumentException("Authentication cookie name must be not empty.");
        this.cookieName = cookieName;
    }

    @Override
    protected String getHeaderName() {
        return HttpHeaders.COOKIE;
    }

    @Override
    protected String getHeaderValue() {
        Object token = context.getAuthenticationToken();
        String cookieValue = token == null ? null : token.toString();
        return cookieName + "=" + cookieValue + ";";
    }
}
