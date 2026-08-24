package com.fintech.common.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaEventProducer {

    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final JsonMapper jsonMapper;

    public KafkaEventProducer(KafkaTemplate<String,Object> kafkaTemplate, JsonMapper jsonMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    public CompletableFuture<SendResult<String, Object>> sendAsync(
            String topic,
            String key,
            String eventType,
            MessageEnvelope<?> payload
    ) {
        return doSend(topic, key, eventType, payload);
    }

    private <T> CompletableFuture<SendResult<String, Object>> doSend(
            String topic,
            String key,
            String eventType,
            MessageEnvelope<T> payload
    ) {
        byte[] authContext = (payload.getAuthContext() == null) ? new byte[0]
                : fetchAuthContext(payload.getAuthContext());
        Message<MessageEnvelope<T>> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader("event-type", eventType)
                .setHeader("auth-context", authContext)
                .build();

        return kafkaTemplate.send(message);
    }

    private byte[] fetchAuthContext (AuthContext ctx) {
        try {
            String json = jsonMapper.writeValueAsString(ctx);
            return json.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize auth context", e);
        }
    }
}
