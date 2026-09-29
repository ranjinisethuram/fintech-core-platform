package com.fintech.common.messaging;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.Map;

public class MessageEnvelope<T> {

    private String messageId;          // Unique per event
    private String eventType;          // ACCOUNT_CREATED
    private String aggregateType;      // ACCOUNT
    private String aggregateId;        // accountId
    private String sagaId;             // Correlates whole saga
    private String correlationId;      // Correlates request chain
    private String causationId;        // Previous messageId
    private Instant occurredAt;        // Event time
    private String version;            // Schema version
    private String sourceService;      // account-service
    private Map<String, String> headers;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class")
    private T payload;
    private AuthContext authContext;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCausationId() {
        return causationId;
    }

    public void setCausationId(String causationId) {
        this.causationId = causationId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public AuthContext getAuthContext() {
        return authContext;
    }

    public void setAuthContext(AuthContext authContext) {
        this.authContext = authContext;
    }

    @Override
    public String toString() {
        return "MessageEnvelope{" +
                "messageId='" + messageId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", aggregateType='" + aggregateType + '\'' +
                ", aggregateId='" + aggregateId + '\'' +
                ", sagaId='" + sagaId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", causationId='" + causationId + '\'' +
                ", occurredAt=" + occurredAt +
                ", version='" + version + '\'' +
                ", sourceService='" + sourceService + '\'' +
                ", headers=" + headers +
                ", payload=" + payload +
                ", authContext=" + authContext +
                '}';
    }
}
