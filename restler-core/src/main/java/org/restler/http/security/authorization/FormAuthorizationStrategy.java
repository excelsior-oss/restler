package org.restler.http.security.authorization;

import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class FormAuthorizationStrategy implements AuthorizationStrategy {

    private static final String COOKIE_HEADER = "Set-Cookie";

    protected String urlString;
    protected String loginParameterName = "j_username";
    protected String loginParameterValue;
    protected String passwordParameterName = "j_password";
    protected String passwordParameterValue;
    protected String cookieName = "JSESSIONID";

    protected RestOperations restOperations;

    public FormAuthorizationStrategy(String url, String login, String password) {
        this.urlString = url;
        this.loginParameterValue = login;
        this.passwordParameterValue = password;
        restOperations = new RestTemplate();
    }

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

        return responseEntity.getHeaders().get(COOKIE_HEADER).stream().filter( s -> s.startsWith(cookieName+"=")).findAny().map( s -> s.split("[=;]")[1]).get();
    }
}
