package com.vhvkhangg.personalprivatevault.authentication.bootstrap;

import com.vhvkhangg.personalprivatevault.authentication.view.AppUserView;

/**
 * Public synchronous capability contract for single-user account bootstrap.
 */
public interface BootstrapOperations {

    boolean isBootstrapped();

    AppUserView bootstrap(BootstrapCommand command);
}
