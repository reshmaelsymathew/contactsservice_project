package com.assignment.contactsservice.config;

import com.assignment.contactsservice.event.ContactCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the KafkaProducerConfig class to verify that the
 * configuration beans are correctly initialized using injected properties.
 * * We use @SpringBootTest to load the application context and @TestPropertySource
 * to override configuration values in application.properties with mock values.
 */
@SpringBootTest(classes = KafkaProducerConfig.class)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092,localhost:9093",
    "app.kafka.topics.contact-event=test_contact_topic"
})
class KafkaProducerConfigTest {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerConfigTest.class);

    // Inject the configured beans directly from the test context
    @Autowired
    private ProducerFactory<String, ContactCreatedEvent> producerFactory;

    @Autowired
    private KafkaTemplate<String, ContactCreatedEvent> kafkaTemplate;

    private final String EXPECTED_BROKERS = "localhost:9092,localhost:9093";
    private final String EXPECTED_TOPIC = "test_contact_topic";

    /**
     * Test case to ensure the producerFactory bean is created successfully 
     * and contains the correct configuration properties.
     */
    @Test
    void producerFactory_isConfiguredCorrectly() {
        log.info("Running test: producerFactory_isConfiguredCorrectly");
        
        assertNotNull(producerFactory, "ProducerFactory should be created by the context.");
        
        Map<String, Object> configs = producerFactory.getConfigurationProperties();

        // 1. Verify Bootstrap Servers
        log.debug("Verifying bootstrap servers: {}", configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(EXPECTED_BROKERS, configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG), 
                     "Bootstrap servers must match the value in @TestPropertySource.");

        // 2. Verify Key Serializer
        log.debug("Verifying key serializer: {}", configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(StringSerializer.class, configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG), 
                     "Key serializer must be StringSerializer.");

        // 3. Verify Value Serializer
        log.debug("Verifying value serializer: {}", configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals(JsonSerializer.class, configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG), 
                     "Value serializer must be JsonSerializer.");
        
        // 4. Verify Type Info Header setting (best practice)
        log.debug("Verifying type info header setting: {}", configs.get(JsonSerializer.ADD_TYPE_INFO_HEADERS));
        assertEquals(false, configs.get(JsonSerializer.ADD_TYPE_INFO_HEADERS), 
                     "JsonSerializer must be configured not to add type headers.");

        log.info("Test passed: ProducerFactory configuration confirmed.");
    }

    /**
     * Test case to ensure the KafkaTemplate bean is created and configured 
     * with the correct ProducerFactory and default topic.
     */
    @Test
    void kafkaTemplate_isConfiguredWithDefaultTopic() {
        log.info("Running test: kafkaTemplate_isConfiguredWithDefaultTopic");
        
        assertNotNull(kafkaTemplate, "KafkaTemplate should be created by the context.");
        
        // 1. Verify it uses the correct ProducerFactory (implicitly checked by Autowired)
        // 2. Verify Default Topic
        log.debug("Verifying default topic: {}", kafkaTemplate.getDefaultTopic());
        assertEquals(EXPECTED_TOPIC, kafkaTemplate.getDefaultTopic(),
                     "KafkaTemplate must have the default topic set from application properties.");
        
        log.info("Test passed: KafkaTemplate default topic confirmed.");
    }
}