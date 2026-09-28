package com.vhvkhangg.personalprivatevault.authentication.session;

import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;

/**
 * Public synchronous capability contract for authentication session lifecycle: login, rotation, and revocation.
 */
public interface SessionOperations {

    AuthTokensView login(LoginCommand command);

    AuthTokensView rotate(String rawRefreshToken);

    void revoke(String rawRefreshToken);
}
