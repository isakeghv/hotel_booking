package com.example.bookingserviceproject.messaging;

import com.example.bookingserviceproject.exception.EventPublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ExecutionException;

/**
 * Sends events to Kafka as JSON text. Used by the publishers, which decide the
 * topic, key and payload.
 */
@Component
public class EventSender {
    private static final Logger log = LoggerFactory.getLogger(EventSender.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    /**
     * Creates the sender with the Kafka client and JSON mapper it uses.
     *
     * @param kafkaTemplate client for sending to Kafka
     * @param jsonMapper    mapper used to convert events to JSON
     */
    public EventSender(KafkaTemplate<String, String> kafkaTemplate, JsonMapper jsonMapper){
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Sends an event and waits for Kafka to confirm it. How long it can take is
     * limited by the producer's timeouts in application.properties (about 5 seconds).
     *
     * @param topic the topic to send to
     * @param key   the key of the event
     * @param event the event, converted to JSON
     * @throws EventPublishException if the event could not be sent
     */
    public void sendAndWait(String topic, String key, Object event){
        try {
            kafkaTemplate.send(topic, key, jsonMapper.writeValueAsString(event)).get();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new EventPublishException(topic, e);
        } catch (ExecutionException | RuntimeException e){
            // RuntimeException: send() itself fails when Kafka cannot be reached within max.block.ms
            throw new EventPublishException(topic, e);
        }
    }

    /**
     * Sends an event without waiting for Kafka to confirm it, for events the
     * response does not depend on. A failure is only logged. send() itself can
     * still block for up to max.block.ms (5 s) if Kafka cannot be reached.
     *
     * @param topic the topic to send to
     * @param key   the key of the event
     * @param event the event, converted to JSON
     */
    public void sendWithoutWaiting(String topic, String key, Object event){
        try {
            kafkaTemplate.send(topic, key, jsonMapper.writeValueAsString(event))
                    .whenComplete((result, e) -> {
                        if (e != null) log.error("Could not send event to {} with key {}", topic, key, e);
                    });
        } catch (RuntimeException e){
            log.error("Could not send event to {} with key {}", topic, key, e);
        }
    }
}
