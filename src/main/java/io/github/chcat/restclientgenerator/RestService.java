package io.github.chcat.restclientgenerator;

import io.github.chcat.restclientgenerator.http.security.SecuritySession;
import io.github.chcat.restclientgenerator.http.security.authorization.AuthorizationStrategy;

/**
 * Created by pasa on 19.02.2015.
 */
public class RestService {

    private String baseUrl;


    public <T extends AuthorizationStrategy> SecuritySession<T> startSession(T authorization){
        SecuritySession<T> session = new SecuritySession<>();
        session.setAuthorization(authorization);
        return session;
    }


}
