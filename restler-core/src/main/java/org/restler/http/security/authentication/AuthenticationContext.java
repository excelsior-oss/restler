package org.restler.http.security.authentication;

public interface AuthenticationContext {

    AuthenticationStrategy getAuthenticationStrategy();

    Object getAuthenticationToken();

    void setAuthenticationToken(Object token);
}
