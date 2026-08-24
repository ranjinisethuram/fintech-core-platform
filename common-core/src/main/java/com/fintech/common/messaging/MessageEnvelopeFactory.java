package com.fintech.common.messaging;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class MessageEnvelopeFactory {

    private final AuthContextProvider authContextProvider;

    private static final String schema_version = "1.0";

    public MessageEnvelopeFactory(AuthContextProvider authContextProvider) {
        this.authContextProvider = authContextProvider;
    }

    public <T extends AggregateMessage> MessageEnvelope<T> build(
            String correlationId,
            String causationId,
            String sagaId,
            String source,
            T payload
    ) {

        MessageEnvelope<T> envelope = new MessageEnvelope<>();

        envelope.setMessageId(UUID.randomUUID().toString());
        envelope.setCorrelationId(correlationId);
        envelope.setEventType(payload.getClass().getSimpleName());
        envelope.setAggregateId(payload.getAggregateId());
        envelope.setAggregateType(payload.getAggregateType().toString());
        envelope.setCausationId(causationId);
        envelope.setSagaId(sagaId);
        envelope.setVersion(schema_version);
        envelope.setSourceService(source);
        envelope.setPayload(payload);
        envelope.setOccurredAt(Instant.now());

        AuthContext authContext = authContextProvider.getSecurityAuthContext();
        envelope.setAuthContext(authContext);

        return envelope;
    }
}
