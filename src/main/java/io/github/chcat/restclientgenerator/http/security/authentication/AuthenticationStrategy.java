package io.github.chcat.restclientgenerator.http.security.authentication;

import org.springframework.http.RequestEntity;

/**
 * Created by pasa on 19.02.2015.
 */
public interface AuthenticationStrategy {

    public void addAuthentication(RequestEntity<?> request);

}
