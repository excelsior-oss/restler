package io.github.chcat.restclientgenerator;

import io.github.chcat.restclientgenerator.http.security.authorization.AuthorizationStrategy;

/**
 * Created by pasa on 19.02.2015.
 */
public class Service {

    private String baseUrl;


    public <T extends AuthorizationStrategy> Session<T> startSession(T authorizationStrategy){
        Session<T> session = new Session<>();
        session.setAuthorizationStrategy(authorizationStrategy);
        return session;
    }


}
