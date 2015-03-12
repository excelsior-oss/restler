package io.github.chcat.restclientgenerator.http.security;

import io.github.chcat.restclientgenerator.http.security.authentication.AuthenticationProvider;
import io.github.chcat.restclientgenerator.http.security.authentication.AuthenticationStrategy;
import io.github.chcat.restclientgenerator.http.security.authorization.AuthorizationStrategy;

/**
 * Created by pasa on 12.03.2015.
 */
public class SecuritySession<T extends AuthorizationStrategy> implements AuthenticationProvider {

    private AuthenticationStrategy authentication;
    private T authorization;

    @Override
    public AuthenticationStrategy provide() {
        return authentication;
    }

    public T getAuthorization(){
        return authorization;
    }

    public void setAuthorization(T authorization){
        this.authorization = authorization;
        authorization.onBindToSession(this);
    }

}
