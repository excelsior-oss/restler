package org.restler.http.security.authentication;

import org.restler.client.Call;

public interface AuthenticationStrategy {

    Call authenticate(Call call, AuthenticationContext context);

}
