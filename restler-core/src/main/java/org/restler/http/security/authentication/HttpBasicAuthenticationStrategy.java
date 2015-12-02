package org.restler.http.security.authentication;

import com.google.common.net.HttpHeaders;
import org.restler.http.Header;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class HttpBasicAuthenticationStrategy extends HeaderBasedAuthenticationStrategy {

    private final String loginValue;
    private final String passwordValue;

    public HttpBasicAuthenticationStrategy(String login, String password) {
        loginValue = login;
        passwordValue = password;
    }

    @Override
    protected List<Header> headers(AuthenticationContext context) {
        return Collections.singletonList(new Header(HttpHeaders.AUTHORIZATION, "Basic " + token()));
    }

    public String token() {
        String loginPassword = loginValue + ":" + passwordValue;
        return Base64.getMimeEncoder().encodeToString(loginPassword.getBytes());
    }

}
