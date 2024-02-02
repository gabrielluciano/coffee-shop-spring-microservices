package com.gabrielluciano.authorizationserver.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientAuthenticationMethodSetConverterTest {

    private final ClientAuthenticationMethodSetConverter converter = new ClientAuthenticationMethodSetConverter();

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
        String result = converter.convertToDatabaseColumn(Set.of(
                ClientAuthenticationMethod.CLIENT_SECRET_JWT));
        assertEquals(ClientAuthenticationMethod.CLIENT_SECRET_JWT.getValue(), result);
    }

    @Test
    void toDatabaseColumnMultipleElements() {
        String result = converter.convertToDatabaseColumn(Set.of(
                ClientAuthenticationMethod.CLIENT_SECRET_JWT, ClientAuthenticationMethod.NONE));

        assertThat(result.split(",")).contains(ClientAuthenticationMethod.CLIENT_SECRET_JWT.getValue());
        assertThat(result.split(",")).contains(ClientAuthenticationMethod.NONE.getValue());
    }

    @Test
    void toEntityAttributeNull() {
        Set<ClientAuthenticationMethod> authenticationMethods = converter.convertToEntityAttribute(null);
        assertThat(authenticationMethods).isEmpty();
    }

    @Test
    void toEntityAttributeEmptyString() {
        Set<ClientAuthenticationMethod> authenticationMethods = converter.convertToEntityAttribute("");
        assertThat(authenticationMethods).isEmpty();
    }

    @Test
    void toEntityAttributeSingleElement() {
        Set<ClientAuthenticationMethod> authenticationMethods = converter.convertToEntityAttribute(
                ClientAuthenticationMethod.CLIENT_SECRET_JWT.getValue());
        assertThat(authenticationMethods).contains(ClientAuthenticationMethod.CLIENT_SECRET_JWT);
    }

    @Test
    void toEntityAttributeMultipleElements() {
        String s = ClientAuthenticationMethod.CLIENT_SECRET_JWT.getValue() + ","
                + ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue();
        Set<ClientAuthenticationMethod> authenticationMethods = converter.convertToEntityAttribute(s);

        assertThat(authenticationMethods).contains(ClientAuthenticationMethod.CLIENT_SECRET_JWT);
        assertThat(authenticationMethods).contains(ClientAuthenticationMethod.CLIENT_SECRET_POST);
    }
}
