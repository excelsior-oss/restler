package org.restler.http.security.authorization;

import java.util.Base64;

public class BasicAuthorizationStrategy implements AuthorizationStrategy {

    private String loginValue;
    private String passwordValue;

    public BasicAuthorizationStrategy(String login, String password) {
        loginValue = login;
        passwordValue = password;
    }

    @Override
    public Object authorize() {
        String loginPassword = loginValue + ":" + passwordValue;

        return Base64.getMimeEncoder().encodeToString(loginPassword.getBytes());
    }
}
