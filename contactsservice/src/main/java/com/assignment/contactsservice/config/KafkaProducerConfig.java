package com.assignment.contactsservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.validation.annotation.Validated;

import com.assignment.contactsservice.event.ContactCreatedEvent;

import jakarta.validation.constraints.NotBlank;

/**
 * Configuration class for setting up the Kafka Producer used to publish
 * ContactCreatedEvent messages to the message broker. This class provides the
 * required ProducerFactory and KafkaTemplate beans.
 */
@Configuration
@Validated // Enables Spring's validation mechanism for this class
public class KafkaProducerConfig {

	private static final Logger log = LoggerFactory.getLogger(KafkaProducerConfig.class);

	// Injects the Kafka broker address from configuration ( application.properties)
	@Value("${spring.kafka.bootstrap-servers}")
	@NotBlank(message = "Kafka bootstrap servers must be configured.")
	private String bootstrapServers;

	// Injects the default topic name for contact events
	@Value("${app.kafka.topics.contact-event}")
	@NotBlank(message = "Contact event topic name must be configured.")
	private String contactEventTopic;

	/**
	 * Configures the Kafka ProducerFactory. This factory holds the configuration
	 * settings for creating Kafka producer instances.
	 * 
	 *
	 * @return The configured ProducerFactory for ContactCreatedEvent messages.
	 */
	@Bean
	public ProducerFactory<String, ContactCreatedEvent> producerFactory() {
		log.info("Configuring Kafka ProducerFactory. Brokers: {}", bootstrapServers);
		Map<String, Object> config = new HashMap<>();

		// Use the injected property value instead of hardcoding
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		// Optional best practice for JSON serialization
		config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

		return new DefaultKafkaProducerFactory<>(config);
	}

	/**
	 * Provides the KafkaTemplate bean for sending messages.
	 *
	 * @return The KafkaTemplate instance configured with the ProducerFactory.
	 */
	@Bean
	public KafkaTemplate<String, ContactCreatedEvent> kafkaTemplate(
			ProducerFactory<String, ContactCreatedEvent> producerFactory) {
		log.info("Initializing KafkaTemplate. Default Topic set to: {}", contactEventTopic);

		// Use the injected ProducerFactory
		KafkaTemplate<String, ContactCreatedEvent> template = new KafkaTemplate<>(producerFactory);

		// Set the default topic, allowing the service layer to use sendDefault()
		template.setDefaultTopic(contactEventTopic);

		log.debug("KafkaTemplate bean creation complete.");
		return template;
	}
}
