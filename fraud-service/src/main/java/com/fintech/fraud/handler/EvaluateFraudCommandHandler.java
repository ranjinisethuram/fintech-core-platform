package com.fintech.fraud.handler;

import com.fintech.common.command.EvaluateFraudCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.fraud.repository.ProcessedMessagesRepository;
import com.fintech.fraud.service.FraudService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class EvaluateFraudCommandHandler implements MessageHandler {

    private final ProcessedMessagesRepository processedMessagesRepository;
    private final FraudService fraudService;

    public EvaluateFraudCommandHandler(ProcessedMessagesRepository processedMessagesRepository, FraudService fraudService) {
        this.processedMessagesRepository = processedMessagesRepository;
        this.fraudService = fraudService;
    }

    @Override
    public String eventType() {
        return "EvaluateFraudCommand";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            EvaluateFraudCommand evaluateFraudCommand = (EvaluateFraudCommand) envelope.getPayload();
            this.fraudService.evaluateCommand(evaluateFraudCommand,
                    envelope.getCorrelationId(),
                    envelope.getCausationId(),
                    envelope.getSagaId());
        }
    }
}
