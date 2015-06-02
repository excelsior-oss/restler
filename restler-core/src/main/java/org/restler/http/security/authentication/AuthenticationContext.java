package org.restler.http.security.authentication;

public interface AuthenticationContext {

    /**
     * Returns the last authentication token assigned to the context.
     *
     * @return a token
     */
    Object getAuthenticationToken();

}
