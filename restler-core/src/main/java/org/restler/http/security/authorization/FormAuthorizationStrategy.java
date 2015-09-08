package org.restler.http.security.authorization;

import com.google.common.net.HttpHeaders;
import org.restler.client.RestlerException;
import org.restler.http.*;

import java.net.URI;
import java.util.stream.Stream;

/**
 * The implementation that performs an HTTP(S) post request of a login form data to obtain a session id.
 */
public class FormAuthorizationStrategy implements AuthorizationStrategy {

    protected final URI urlString;
    protected final String loginParameterName;
    protected final String loginParameterValue;
    protected final String passwordParameterName;
    protected final String passwordParameterValue;
    protected final String cookieName = "JSESSIONID";

    private final RequestExecutor requestExecutor;

    /**
     * Creates a strategy that uses custom parameter names.
     *
     * @param url                   url of the login service.
     * @param login                 login of the user
     * @param loginParameterName    name of the form parameter holding the <tt>login</tt> value
     * @param password              password of the user
     * @param passwordParameterName name of the form parameter holding the <tt>password</tt> value
     */
    public FormAuthorizationStrategy(RequestExecutor requestExecutor, URI url, String login, String loginParameterName, String password, String passwordParameterName) {
        this.requestExecutor = requestExecutor;
        this.urlString = url;
        this.loginParameterValue = login;
        this.passwordParameterValue = password;
        this.loginParameterName = loginParameterName;
        this.passwordParameterName = passwordParameterName;
    }

    @Override
    public Object authorize() {
        HttpForm form = new HttpForm().
                add(loginParameterName, loginParameterValue).
                add(passwordParameterName, passwordParameterValue);

        Response<?> response = requestExecutor.execute(new Request<>(urlString, HttpMethod.POST, form, Object.class));

        Stream<String> headers = response.getHeaders().get(HttpHeaders.SET_COOKIE).stream();
        return headers.filter(s -> s.startsWith(cookieName + "=")).
                findAny().
                map(s -> s.split("[=;]")[1]).
                orElseThrow(() -> new RestlerException("Cookie " + cookieName + " not found in response on authorization request"));
    }

}
