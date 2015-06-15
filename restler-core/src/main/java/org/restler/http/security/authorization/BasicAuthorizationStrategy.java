package org.restler.http.security.authorization;

import java.util.Base64;

/**
 * Created by oleg on 15.06.2015.
 */
public class BasicAuthorizationStrategy implements AuthorizationStrategy {

    protected String loginValue;
    protected String passwordValue;

    public BasicAuthorizationStrategy(String login, String password) {
        loginValue = login;
        passwordValue = password;
    }

    @Override
    public Object authorize() {
        String loginPassword = loginValue + ":" + passwordValue;
        String loginPasswordBase64 = Base64.getMimeEncoder().encodeToString(loginPassword.getBytes());

        return loginPasswordBase64;
    }
}
