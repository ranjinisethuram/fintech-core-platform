package com.fintech.ledger.handler;

import com.fintech.common.command.CreateLedgerEntryCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.ledger.repository.ProcessedMessagesRepository;
import com.fintech.ledger.service.LedgerService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class CreateLedgerEntryCommandHandler implements MessageHandler {

    private final LedgerService ledgerService;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public CreateLedgerEntryCommandHandler(LedgerService ledgerService, ProcessedMessagesRepository processedMessagesRepository) {
        this.ledgerService = ledgerService;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "CreateLedgerEntryCommand";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            CreateLedgerEntryCommand createLedgerEntryCommand =
                    (CreateLedgerEntryCommand)envelope.getPayload();
            this.ledgerService.createLedgerEntries(createLedgerEntryCommand,
                    envelope.getCorrelationId(),
                    envelope.getMessageId(),
                    envelope.getSagaId());
        }
    }
}
