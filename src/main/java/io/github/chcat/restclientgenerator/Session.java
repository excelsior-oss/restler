package io.github.chcat.restclientgenerator;

import io.github.chcat.restclientgenerator.http.security.authentication.AuthenticationProvider;
import io.github.chcat.restclientgenerator.http.security.authentication.AuthenticationStrategy;
import io.github.chcat.restclientgenerator.http.security.authorization.AuthorizationStrategy;

/**
 * Created by pasa on 12.03.2015.
 */
public class Session<T extends AuthorizationStrategy> implements AuthenticationProvider {

    private AuthenticationStrategy authenticationStrategy;
    private T authorizationStrategy;

    @Override
    public AuthenticationStrategy provideAuthenticationStrategy() {
        return authenticationStrategy;
    }

    public T getAuthorizationStrategy(){
        return authorizationStrategy;
    }

    public void setAuthorizationStrategy(T authorizationStrategy){
        if (this.authorizationStrategy != null){
            this.authorizationStrategy.onUnbindFromSession(this);
        }

        this.authorizationStrategy = authorizationStrategy;
        authorizationStrategy.onBindToSession(this);
    }


}
