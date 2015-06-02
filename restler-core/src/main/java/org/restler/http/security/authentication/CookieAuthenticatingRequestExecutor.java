package org.restler.http.security.authentication;

import org.restler.http.ExecutableRequest;
import org.restler.http.HttpRequestExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

/**
 * The implementation that authenticates a request adding a cookie.
 */
public class CookieAuthenticatingRequestExecutor implements HttpRequestExecutor {

    protected static final String JSESSIONID = "JSESSIONID";

    private final String cookieName;
    private final AuthenticationContext context;
    private final HttpRequestExecutor executor;

    /**
     * Creates the strategy that uses JSESSIONID cookie.
     *
     * @param executor
     * @param context
     */
    public CookieAuthenticatingRequestExecutor(HttpRequestExecutor executor, AuthenticationContext context) {
        this(JSESSIONID, context, executor);
    }

    /**
     * Creates the strategy that uses a custom cookie.
     *
     * @param cookieName the name of the cookie.
     * @param context
     * @param executor
     */
    public CookieAuthenticatingRequestExecutor(String cookieName, AuthenticationContext context, HttpRequestExecutor executor) {
        this.context = context;
        this.executor = executor;
        if (StringUtils.isEmpty(cookieName))
            throw new IllegalArgumentException("Authentication cookie name must be not empty.");
        this.cookieName = cookieName;
    }

    @Override
    public <T> ResponseEntity<T> execute(ExecutableRequest<T> executableRequest) {
        Object token = context.getAuthenticationToken();
        String cookieValue = token == null ? null : token.toString();
        ExecutableRequest<T> authenticatedRequest = executableRequest;
        if (!StringUtils.isEmpty(cookieValue)) {
            authenticatedRequest = executableRequest.setHeader(HttpHeaders.COOKIE, cookieName + "=" + cookieValue + ";");
        }
        return executor.execute(authenticatedRequest);
    }
}
