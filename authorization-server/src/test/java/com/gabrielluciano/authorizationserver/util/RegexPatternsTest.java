package com.gabrielluciano.authorizationserver.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class RegexPatternsTest {

    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(RegexPatterns.STRONG_PASSWORD_PATTERN);

    @Test
    void shouldMatchPasswords() {
        assertThat("Password@0").matches(STRONG_PASSWORD_PATTERN);
        assertThat("Password;1").matches(STRONG_PASSWORD_PATTERN);
    }

    @Test
    void shouldNotMatchPasswords() {
        assertThat("").doesNotMatch(STRONG_PASSWORD_PATTERN);
        assertThat("Pass@0").doesNotMatch(STRONG_PASSWORD_PATTERN);     // Less than 8 characters
        assertThat("Password0").doesNotMatch(STRONG_PASSWORD_PATTERN);  // No special characters
        assertThat("Password@").doesNotMatch(STRONG_PASSWORD_PATTERN);  // No digit
        assertThat("password@0").doesNotMatch(STRONG_PASSWORD_PATTERN); // No uppercase letter
        assertThat("PASSWORD@0").doesNotMatch(STRONG_PASSWORD_PATTERN); // No lowercase letter
    }
}
