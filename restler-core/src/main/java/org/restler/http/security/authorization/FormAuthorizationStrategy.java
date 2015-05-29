package org.restler.http.security.authorization;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The implementation that performs an HTTP(S) post request of a login form data to obtain a session id.
 */
public class FormAuthorizationStrategy implements AuthorizationStrategy {

    protected String urlString;
    protected String loginParameterName = "j_username";
    protected String loginParameterValue;
    protected String passwordParameterName = "j_password";
    protected String passwordParameterValue;
    protected String cookieName = "JSESSIONID";

    protected RestOperations restOperations;

    /**
     * Creates a strategy that uses default parameter names.
     * @param url url of the login service.
     * @param login login of the user to be passed as <tt>j_username</tt>
     * @param password password of the user to be passed as <tt>j_password</tt>
     */
    public FormAuthorizationStrategy(String url, String login, String password) {
        this.urlString = url;
        this.loginParameterValue = login;
        this.passwordParameterValue = password;
        restOperations = new RestTemplate();
    }

    /**
     * Creates a strategy that uses custom parameter names.
     * @param url url of the login service.
     * @param login login of the user
     * @param loginParameterName name of the form parameter holding the <tt>login</tt> value
     * @param password password of the user
     * @param passwordParameterName name of the form parameter holding the <tt>password</tt> value
     */
    public FormAuthorizationStrategy(String url, String login, String loginParameterName, String password, String passwordParameterName) {
        this(url,login,password);
        this.loginParameterName = loginParameterName;
        this.passwordParameterName = passwordParameterName;
    }

    @Override
    public Object authorize() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add(loginParameterName, loginParameterValue);
        params.add(passwordParameterName, passwordParameterValue);

        ResponseEntity<?> responseEntity = restOperations.postForEntity(UriComponentsBuilder.fromUriString(urlString).build().toUri(),params,Object.class);

        return responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE).stream().filter( s -> s.startsWith(cookieName+"=")).findAny().map( s -> s.split("[=;]")[1]).get();
    }
}
