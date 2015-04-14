package io.github.chcat.restclientgenerator.http.security.authentication;

/**
 * Created by pasa on 12.03.2015.
 */
public interface AuthenticationProvider {

    public AuthenticationStrategy provideAuthenticationStrategy();
}
