package org.restler.http.security.authentication;

import org.restler.client.HttpCall;

public interface AuthenticationStrategy {

    <T> HttpCall<T> authenticate(HttpCall<T> call, AuthenticationContext context);

}
