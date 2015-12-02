package org.restler.http.security.authentication;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import org.restler.http.Header;

import java.util.Collections;
import java.util.List;

/**
 * The implementation that authenticates a request adding a cookie.
 */
public class CookieAuthenticationStrategy extends HeaderBasedAuthenticationStrategy {

    public static final String JSESSIONID = "JSESSIONID";

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
        if (Strings.isNullOrEmpty(cookieName))
            throw new IllegalArgumentException("Authentication cookie name must be not empty.");
        this.cookieName = cookieName;
    }

    @Override
    protected List<Header> headers(AuthenticationContext context) {
        return Collections.singletonList(new Header(HttpHeaders.COOKIE, value(context)));
    }

    private String value(AuthenticationContext context) {
        Object token = context.getAuthenticationToken();
        String cookieValue = token == null ? null : token.toString();
        return cookieName + "=" + cookieValue + ";";
    }

}
