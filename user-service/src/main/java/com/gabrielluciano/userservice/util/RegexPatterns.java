package com.gabrielluciano.userservice.util;

public class RegexPatterns {

    /**
     * A regular expression pattern that matches a strong password.
     *
     * <p>The regular expression pattern matches any string that:</p>
     * <ul>
     *   <li>Has a minimum length of 8 characters</li>
     *   <li>Contains at least one uppercase letter, one lowercase letter, one digit, and one special character</li>
     *   <li>Allows the following special characters: ! @ # $ % ^ & * ( ) _ + - = { } [ ] ; : ' " , &lt; . &gt; / ?</li>
     * </ul>
     *
     * <p>The regular expression pattern is:</p>
     * <pre>{@code "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_\\+\\-=\\{\\}\\[\\];:'\",\\<.\\>/?]).{8,}$"}</pre>
     *
     * <p>The pattern consists of the following parts:</p>
     * <ul>
     *     <li>{@code ^} matches the start of the string.</li>
     *     <li>{@code (?=.*[A-Z])} matches any string that contains at least one uppercase letter.</li>
     *     <li>{@code (?=.*[a-z])} matches any string that contains at least one lowercase letter.</li>
     *     <li>{@code (?=.*[0-9])} matches any string that contains at least one digit.</li>
     *     <li>{@code (?=.*[!@#$%^&*()_\\+\\-=\\{\\}\\[\\];:'\",\\<.\\>/?])} matches any string that contains at least one of
     *     the allowed special characters.</li>
     *     <li>{@code .{8,}} matches any string that is at least 8 characters long.</li>
     *     <li>{@code $} matches the end of the string.</li>
     * </ul>
     *
     * <p>Examples of strings that match the pattern include:</p>
     * <ul>
     *     <li>"Password1!"</li>
     *     <li>"P@ssword123"</li>
     *     <li>"mySecret&"</li>
     * </ul>
     *
     * <p>Examples of strings that do not match the pattern include:</p>
     * <ul>
     *     <li>"password" (no uppercase letter)</li>
     *     <li>"PASSWORD" (no lowercase letter)</li>
     *     <li>"Password" (no digit)</li>
     *     <li>"Pass1!" (less than 8 characters long)</li>
     *     <li>"Password123" (no special character)</li>
     *     <li>"password&" (no uppercase letter or digit)</li>
     * </ul>
     */
    public static final String STRONG_PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_\\+\\-=\\{\\}\\[\\];:'\",\\<.\\>/?]).{8,}$";
}
