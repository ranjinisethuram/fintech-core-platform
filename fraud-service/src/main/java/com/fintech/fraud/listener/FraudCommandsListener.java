package com.fintech.fraud.listener;

import com.fintech.common.command.EvaluateFraudCommand;
import com.fintech.common.messaging.MessageDispatcher;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.fraud.service.FraudService;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.stereotype.Component;

@Component
public class FraudCommandsListener {

    private final MessageDispatcher commandDispatcher;

    public FraudCommandsListener(MessageDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @RetryableTopic(attempts = "3",backOff = @BackOff(delay = 3000, multiplier = 2.0),
            include = {TransientDataAccessException.class, DataAccessResourceFailureException.class},
            //exclude = {DataIntegrityViolationException.class, DeserializationException.class, java.lang.IllegalArgumentException.class, java.lang.NullPointerException.class},
            dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "fraud-commands", groupId = "fraud-service", containerFactory = "kafkaListenerContainerFactory")
    public void listen(MessageEnvelope<?> envelope) {
        commandDispatcher.dispatch(envelope);
    }
}
