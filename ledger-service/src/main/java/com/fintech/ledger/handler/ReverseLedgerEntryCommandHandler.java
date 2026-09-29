package com.fintech.ledger.handler;

import com.fintech.common.command.ReverseLedgerEntryCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.ledger.repository.ProcessedMessagesRepository;
import com.fintech.ledger.service.LedgerService;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ReverseLedgerEntryCommandHandler implements MessageHandler {

    private final LedgerService ledgerService;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public ReverseLedgerEntryCommandHandler(LedgerService ledgerService, ProcessedMessagesRepository processedMessagesRepository) {
        this.ledgerService = ledgerService;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "ReverseLedgerEntryCommand";
    }

    @Override
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0) {
            ReverseLedgerEntryCommand reverseLedgerEntryCommand =
                    (ReverseLedgerEntryCommand) envelope.getPayload();
            this.ledgerService.reverseLedgerEntries(reverseLedgerEntryCommand,
                    envelope.getCorrelationId(),
                    envelope.getMessageId(),
                    envelope.getSagaId());
        }
    }
}
