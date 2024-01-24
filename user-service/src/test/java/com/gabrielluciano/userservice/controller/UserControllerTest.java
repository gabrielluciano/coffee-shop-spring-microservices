package com.gabrielluciano.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielluciano.userservice.dto.SignupRequest;
import com.gabrielluciano.userservice.model.Role;
import com.gabrielluciano.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .name("John")
                .email("john@email.com")
                .password("Passw0rd!")
                .build();

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(signupRequest.getName()))
                .andExpect(jsonPath("$.email").value(signupRequest.getEmail()))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value(Role.USER.name()));
    }

    @Test
    void shouldReturn409OnDuplicateEmail() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .name("John")
                .email("john@email.com")
                .password("Passw0rd!")
                .build();

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andDo(print());

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.path").value("/api/v1/user"));
    }

    @Test
    void shouldReturn400WhenNameIsNull() throws Exception {
        shouldReturn400BadRequestWhenNameIs(null);
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        shouldReturn400BadRequestWhenNameIs("");
    }

    @Test
    void shouldReturn400WhenNameIsGreaterThan100() throws Exception {
        int maxLength = 100;
        shouldReturn400BadRequestWhenNameIs("#".repeat(maxLength + 1));
    }

    @Test
    void shouldReturn400WhenEmailIsNull() throws Exception {
        shouldReturn400BadRequestWhenEmailIs(null);
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        shouldReturn400BadRequestWhenEmailIs("");
        shouldReturn400BadRequestWhenEmailIs("invalid");
        shouldReturn400BadRequestWhenEmailIs("@");
        shouldReturn400BadRequestWhenEmailIs("user@");
        shouldReturn400BadRequestWhenEmailIs("user@.com");
        shouldReturn400BadRequestWhenEmailIs("@example.com");
    }

    @Test
    void shouldReturn400WhenPasswordIsNull() throws Exception {
        shouldReturn400BadRequestWhenPasswordIs(null);
    }

    @Test
    void shouldReturn400WhenPasswordIsInvalid() throws Exception {
        shouldReturn400BadRequestWhenPasswordIs("");
        shouldReturn400BadRequestWhenPasswordIs("password");    // no uppercase letter
        shouldReturn400BadRequestWhenPasswordIs("PASSWORD");    // no lowercase letter
        shouldReturn400BadRequestWhenPasswordIs("Password");    // no digit
        shouldReturn400BadRequestWhenPasswordIs("Pass1!");      // less than 8 characters long
        shouldReturn400BadRequestWhenPasswordIs("Password123"); // no special character
        shouldReturn400BadRequestWhenPasswordIs("password&");   // no uppercase letter or digit
    }

    private void shouldReturn400BadRequestWhenNameIs(String name) throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .name(name)
                .email("john@email.com")
                .password("Passw0rd!")
                .build();

        performSignupRequestAndExpect400ErrorWithErrorMessageContaining(signupRequest, "name");
    }

    private void shouldReturn400BadRequestWhenEmailIs(String email) throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .name("John")
                .email(email)
                .password("Passw0rd!")
                .build();

        performSignupRequestAndExpect400ErrorWithErrorMessageContaining(signupRequest, "email");
    }

    private void shouldReturn400BadRequestWhenPasswordIs(String password) throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .name("John")
                .email("john@email.com")
                .password(password)
                .build();

        performSignupRequestAndExpect400ErrorWithErrorMessageContaining(signupRequest, "password");
    }

    private void performSignupRequestAndExpect400ErrorWithErrorMessageContaining(
            SignupRequest signupRequest, String expectedValue) throws Exception {

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.path").value("/api/v1/user"))
                .andExpect(jsonPath("$.error", containsString(expectedValue)));
    }

    static String asJsonString(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }
}
