package com.gabrielluciano.authorizationserver.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorizationGrantTypeSetConverterTest {

    private final AuthorizationGrantTypeSetConverter converter = new AuthorizationGrantTypeSetConverter();

    @Test
    void toDatabaseColumnNull() {
        String emptyString = "";
        String result = converter.convertToDatabaseColumn(null);
        assertEquals(emptyString, result);
    }

    @Test
    void toDatabaseColumnEmptySet() {
        String emptyString = "";
        String result = converter.convertToDatabaseColumn(Collections.emptySet());
        assertEquals(emptyString, result);
    }

    @Test
    void toDatabaseColumnSingleElement() {
        String result = converter.convertToDatabaseColumn(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE));
        assertEquals(AuthorizationGrantType.AUTHORIZATION_CODE.getValue(), result);
    }

    @Test
    void toDatabaseColumnMultipleElements() {
        String result = converter.convertToDatabaseColumn(Set.of(
                AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN));

        assertThat(result.split(",")).contains(AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
        assertThat(result.split(",")).contains(AuthorizationGrantType.REFRESH_TOKEN.getValue());
    }

    @Test
    void toEntityAttributeNull() {
        Set<AuthorizationGrantType> grantTypes = converter.convertToEntityAttribute(null);
        assertThat(grantTypes).isEmpty();
    }

    @Test
    void toEntityAttributeEmptyString() {
        Set<AuthorizationGrantType> grantTypes = converter.convertToEntityAttribute("");
        assertThat(grantTypes).isEmpty();
    }

    @Test
    void toEntityAttributeSingleElement() {
        Set<AuthorizationGrantType> grantTypes = converter.convertToEntityAttribute(
                AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
        assertThat(grantTypes).contains(AuthorizationGrantType.AUTHORIZATION_CODE);
    }

    @Test
    void toEntityAttributeMultipleElements() {
        String s = AuthorizationGrantType.CLIENT_CREDENTIALS.getValue() + ","
                + AuthorizationGrantType.AUTHORIZATION_CODE.getValue();
        Set<AuthorizationGrantType> grantTypes = converter.convertToEntityAttribute(s);

        assertThat(grantTypes).contains(AuthorizationGrantType.CLIENT_CREDENTIALS);
        assertThat(grantTypes).contains(AuthorizationGrantType.AUTHORIZATION_CODE);
    }
}
