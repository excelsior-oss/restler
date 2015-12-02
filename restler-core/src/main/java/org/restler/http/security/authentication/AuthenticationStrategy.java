package org.restler.http.security.authentication;

import org.restler.client.Call;

/**
 * Authentication strategy is responsible for calls authentication, enriching them in some way with token obtained by the authorization strategy.
 * Implementations of this interface should be stateless, since they may be reused between different proxies and services.
 */
public interface AuthenticationStrategy {

    Call authenticate(Call call, AuthenticationContext context);

}
