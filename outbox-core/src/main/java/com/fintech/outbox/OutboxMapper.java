package com.fintech.outbox;

import com.fintech.common.messaging.MessageEnvelope;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class OutboxMapper {

    private final JsonMapper jsonMapper;

    public OutboxMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public <T, E extends BaseOutboxEvent> E mapToOutboxEvent(
            MessageEnvelope<T> envelope,
            Supplier<E> entitySupplier
    ) {

        E entity = entitySupplier.get();

        try {
//            String serialized = objectMapper.writeValueAsString(envelope);
            entity.setAggregateType(envelope.getAggregateType());
            entity.setAggregateId(envelope.getAggregateId());
            entity.setEventType(envelope.getEventType());
            entity.setSagaId(envelope.getSagaId());
            entity.setCorrelationId(envelope.getCorrelationId());
            entity.setStatus(OutboxStatus.PENDING);
            entity.setCreatedAt(Instant.now());
            entity.setPayload(envelope);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize MessageEnvelope", e);
        }

        return entity;
    }

    public <T> MessageEnvelope<T> mapToEnvelope(
            String payload,
            Class<T> payloadType
    ) {
        try {
            JavaType type = jsonMapper.getTypeFactory()
                    .constructParametricType(MessageEnvelope.class, payloadType);

            return jsonMapper.readValue(payload, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize payload", e);
        }
    }

    public <T> String serializeMessageEnvelopePayload(T messageEnvelopePayload){
        try {
            return jsonMapper.writeValueAsString(messageEnvelopePayload);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize payload",e);
        }
    }
}
