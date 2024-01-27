package com.gabrielluciano.authorizationserver.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
public class OAuthScope {

    public static final String OPENID = "openid";
    public static final String PROFILE = "profile";
    public static final String EMAIL = "email";
    public static final String ADDRESS = "address";

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
