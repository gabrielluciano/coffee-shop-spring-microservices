package com.gabrielluciano.authorizationserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielluciano.authorizationserver.dto.UserRegistrationRequest;
import com.gabrielluciano.authorizationserver.events.UserRegisteredEvent;
import com.gabrielluciano.authorizationserver.model.Role;
import com.gabrielluciano.authorizationserver.repository.UserCredentialsRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@EmbeddedKafka(topics = "user-registration-events")
class UserCredentialsControllerTest {

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
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @BeforeEach
    void setUp() {
        userCredentialsRepository.deleteAll();
    }

    @Test
    @DirtiesContext
    void shouldRegisterUser() throws Exception {
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .name("John")
                .email("john@email.com")
                .password("Passw0rd!")
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRegistrationRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(userRegistrationRequest.getName()))
                .andExpect(jsonPath("$.email").value(userRegistrationRequest.getEmail()))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value(Role.USER.name()));
    }

    @Test
    @DirtiesContext
    void shouldSendUserRegisteredEvent() throws Exception {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        ConsumerFactory<String, UserRegisteredEvent> cf = new DefaultKafkaConsumerFactory<>(consumerProps,
                new StringDeserializer(), new JsonDeserializer<>(UserRegisteredEvent.class));
        Consumer<String, UserRegisteredEvent> consumer = cf.createConsumer();
        this.embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "user-registration-events");

        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .name("Mark")
                .email("mark@email.com")
                .password("Passw0rd!")
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRegistrationRequest)))
                .andDo(print());

        ConsumerRecords<String, UserRegisteredEvent> records = KafkaTestUtils.getRecords(consumer);
        UserRegisteredEvent event = records.iterator().next().value();

        assertThat(records.count()).isEqualTo(1);
        assertThat(event.getEventType()).isEqualTo("UserRegisteredEvent");
        assertThat(event.getName()).isEqualTo(userRegistrationRequest.getName());
    }

    @Test
    void shouldReturn409OnDuplicatedEmail() throws Exception {
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .name("John")
                .email("john@email.com")
                .password("Passw0rd!")
                .build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRegistrationRequest)))
                .andDo(print());

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRegistrationRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.path").value("/register"));
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
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .name(name)
                .email("john@email.com")
                .password("Passw0rd!")
                .build();

        performUserRegistrationRequestAndExpect400ErrorWithErrorMessageContaining(userRegistrationRequest, "name");
    }

    private void shouldReturn400BadRequestWhenEmailIs(String email) throws Exception {
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .name("John")
                .email(email)
                .password("Passw0rd!")
                .build();

        performUserRegistrationRequestAndExpect400ErrorWithErrorMessageContaining(userRegistrationRequest, "email");
    }

    private void shouldReturn400BadRequestWhenPasswordIs(String password) throws Exception {
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .name("John")
                .email("john@email.com")
                .password(password)
                .build();

        performUserRegistrationRequestAndExpect400ErrorWithErrorMessageContaining(userRegistrationRequest, "password");
    }

    private void performUserRegistrationRequestAndExpect400ErrorWithErrorMessageContaining(
            UserRegistrationRequest userRegistrationRequest, String expectedValue) throws Exception {

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRegistrationRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.path").value("/register"))
                .andExpect(jsonPath("$.error", containsString(expectedValue)));
    }

    static String asJsonString(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }
}
