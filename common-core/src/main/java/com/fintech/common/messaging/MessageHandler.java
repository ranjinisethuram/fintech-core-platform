package com.fintech.common.messaging;

public interface MessageHandler {

    String eventType();
    void handle(MessageEnvelope<?> envelope);
}
