package com.gabrielluciano.authorizationserver.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class OAuthScope {

    public static final String OPENID = "openid";
    public static final String PROFILE = "profile";
    public static final String EMAIL = "email";
    public static final String ADDRESS = "address";

    private final String value;
}
