package com.vhvkhangg.personalprivatevault.authentication.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.AppUserResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.AuthTokensResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.BootstrapRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.ChangePinRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.LoginRequest;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.ChangePinCommand;
import com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand;
import com.vhvkhangg.personalprivatevault.authentication.view.AppUserView;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;

public final class AuthWebMapper {

    private AuthWebMapper() {}

    public static BootstrapCommand toCommand(BootstrapRequest request) {
        return new BootstrapCommand(
                request.email(),
                request.username(),
                request.password(),
                request.pin()
        );
    }

    public static AppUserResponse toResponse(AppUserView view) {
        return new AppUserResponse(
                view.id(),
                view.email(),
                view.username(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    public static LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(
                request.identifier(),
                request.password()
        );
    }

    public static AuthTokensResponse toResponse(AuthTokensView view) {
        return new AuthTokensResponse(
                view.accessToken(),
                view.refreshToken(),
                view.tokenType(),
                view.expiresIn()
        );
    }

    public static ChangePinCommand toCommand(ChangePinRequest request) {
        return new ChangePinCommand(
                request.currentPin(),
                request.newPin()
        );
    }
}
