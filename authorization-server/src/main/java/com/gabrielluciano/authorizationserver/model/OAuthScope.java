package com.gabrielluciano.authorizationserver.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class OAuthScope {

    public static final OAuthScope OPENID = new OAuthScope("openid");
    public static final OAuthScope PROFILE = new OAuthScope("profile");
    public static final OAuthScope EMAIL = new OAuthScope("email");
    public static final OAuthScope ADDRESS = new OAuthScope("address");

    private final String value;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        OAuthScope that = (OAuthScope) object;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
