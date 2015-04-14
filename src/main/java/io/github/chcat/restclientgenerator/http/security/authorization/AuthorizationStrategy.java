package io.github.chcat.restclientgenerator.http.security.authorization;

import io.github.chcat.restclientgenerator.Session;

/**
 * Created by ChCat on 20.02.2015.
 */
public interface AuthorizationStrategy {

    public void onBindToSession(Session session);

    public void onUnbindFromSession(Session session);

}
