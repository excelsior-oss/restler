package org.restler.http.security.authentication;

import org.restler.http.Request;

public interface AuthenticationStrategy {

    <T> Request<T> authenticate(Request<T> request, AuthenticationContext context);

}
