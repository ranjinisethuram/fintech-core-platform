package com.fintech.common.messaging;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MessageDispatcher {

    private final Map<String, MessageHandler> handlers;

    public MessageDispatcher(List<MessageHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        MessageHandler::eventType,
                        Function.identity()
                ));
    }

    public void dispatch(MessageEnvelope<?> envelope) {
        MessageHandler handler = handlers.get(envelope.getEventType());
        if (handler == null) {
            return;
        }

        handler.handle(envelope);
    }
}
