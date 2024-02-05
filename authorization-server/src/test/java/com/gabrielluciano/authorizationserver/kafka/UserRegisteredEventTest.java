package com.gabrielluciano.authorizationserver.kafka;

import com.gabrielluciano.authorizationserver.dto.UserRegistrationRequest;
import com.gabrielluciano.authorizationserver.event.UserRegisteredEvent;
import com.gabrielluciano.authorizationserver.repository.UserCredentialsRepository;
import com.gabrielluciano.authorizationserver.service.UserCredentialsServiceImpl;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserRegisteredEventTest.TestConfig.class, UserCredentialsServiceImpl.class})
@EmbeddedKafka(topics = "user-registration-events")
@DirtiesContext
class UserRegisteredEventTest {

    @Autowired
    private UserCredentialsServiceImpl userCredentialsService;

    @MockBean
    private UserCredentialsRepository userCredentialsRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @BeforeEach
    void setUp() {
        BDDMockito.when(passwordEncoder.encode(ArgumentMatchers.anyString()))
                .thenReturn("encodedPassword");
    }

    @Test
    void shouldSendUserRegisteredEvent() {
        Consumer<String, UserRegisteredEvent> consumer = createConsumer();
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .name("Mark")
                .email("mark@email.com")
                .password("Passw0rd!")
                .build();

        userCredentialsService.registerUser(userRegistrationRequest);

        ConsumerRecords<String, UserRegisteredEvent> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        UserRegisteredEvent event = records.iterator().next().value();

        assertThat(records.count()).isEqualTo(1);
        assertThat(event.getEventType()).isEqualTo("UserRegisteredEvent");
        assertThat(event.getName()).isEqualTo(userRegistrationRequest.getName());
    }

    @Test
    void shouldNotSendUserRegisteredEvent() {
        BDDMockito.when(userCredentialsRepository.save(ArgumentMatchers.any()))
                .thenThrow(RuntimeException.class);
        Consumer<String, UserRegisteredEvent> consumer = createConsumer();
        UserRegistrationRequest userRegistrationRequest = UserRegistrationRequest.builder()
                .name("Mark")
                .email("mark@email.com")
                .password("Passw0rd!")
                .build();

        assertThatRuntimeException()
                .isThrownBy(() -> userCredentialsService.registerUser(userRegistrationRequest));

        ConsumerRecords<String, UserRegisteredEvent> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));

        assertThat(records.count()).isEqualTo(0);
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> records.iterator().next());
    }

    private Consumer<String, UserRegisteredEvent> createConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TYPE_MAPPINGS, "event:com.gabrielluciano.authorizationserver.event.UserRegisteredEvent");
        ConsumerFactory<String, UserRegisteredEvent> cf = new DefaultKafkaConsumerFactory<>(consumerProps,
                new StringDeserializer(), new JsonDeserializer<>(UserRegisteredEvent.class));
        Consumer<String, UserRegisteredEvent> consumer = cf.createConsumer();
        this.embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "user-registration-events");
        return consumer;
    }

    @Configuration
    @EnableKafka
    static class TestConfig {

        @Value("${" + EmbeddedKafkaBroker.SPRING_EMBEDDED_KAFKA_BROKERS + "}")
        private String brokerAddresses;

        @Bean
        public ProducerFactory<String, UserRegisteredEvent> userRegisteredEventProducerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddresses);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            props.put(JsonSerializer.TYPE_MAPPINGS, "event:com.gabrielluciano.authorizationserver.event.UserRegisteredEvent");
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        public KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate() {
            return new KafkaTemplate<>(userRegisteredEventProducerFactory());
        }
    }
}
