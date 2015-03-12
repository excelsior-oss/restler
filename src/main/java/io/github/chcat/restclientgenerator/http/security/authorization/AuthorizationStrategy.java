package io.github.chcat.restclientgenerator.http.security.authorization;

import io.github.chcat.restclientgenerator.http.security.SecuritySession;

/**
 * Created by ChCat on 20.02.2015.
 */
public interface AuthorizationStrategy {

    public void onBindToSession(SecuritySession session);

}
