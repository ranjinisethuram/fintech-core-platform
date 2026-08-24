package com.fintech.orchestration.handler.transaction;

import com.fintech.common.domain.TransactionType;
import com.fintech.common.event.TransactionInitiatedEvent;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.orchestration.contextmapper.TransactionHandlingContext;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContextType;
import com.fintech.orchestration.engine.OrchestrationEngine;
import com.fintech.orchestration.engine.SagaLifeCycleManager;
import com.fintech.orchestration.repository.ProcessedMessagesRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class TransactionInitiatedEventHandler implements MessageHandler {

    private final ProcessedMessagesRepository processedMessagesRepository;
    private final SagaLifeCycleManager sagaLifeCycleManager;
    private final OrchestrationEngine orchestrationEngine;

    public TransactionInitiatedEventHandler(ProcessedMessagesRepository processedMessagesRepository, SagaLifeCycleManager sagaLifeCycleManager, OrchestrationEngine orchestrationEngine) {
        this.sagaLifeCycleManager = sagaLifeCycleManager;
        this.orchestrationEngine = orchestrationEngine;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "TransactionInitiatedEvent";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            TransactionInitiatedEvent transactionInitiatedEvent = (TransactionInitiatedEvent)
                    envelope.getPayload();
            TransactionHandlingContext transactionHandlingContext = getTransactionHandlingContext(transactionInitiatedEvent,
                    transactionInitiatedEvent.transactionType());
            Saga sagaSaved = this.sagaLifeCycleManager.initiateSaga(envelope,
                    SagaContextType.TRANSACTION_HANDLING,transactionHandlingContext);
            this.orchestrationEngine.process(sagaSaved, envelope.getMessageId());
        }
    }

    private TransactionHandlingContext getTransactionHandlingContext(
            TransactionInitiatedEvent transactionInitiatedEvent,
            TransactionType transactionType){
        TransactionHandlingContext transactionHandlingContext = new TransactionHandlingContext();
        transactionHandlingContext.setTransactionId(transactionInitiatedEvent.transactionId());
        transactionHandlingContext.setAmount(transactionInitiatedEvent.amount());
        transactionHandlingContext.setCurrency(transactionInitiatedEvent.currency());
        transactionHandlingContext.setSourceAccountId(transactionInitiatedEvent.sourceAccountId());
        transactionHandlingContext.setDestinationAccountId(transactionInitiatedEvent.destinationAccountId());
        transactionHandlingContext.setTransactionType(transactionType);
        return transactionHandlingContext;
    }

}
