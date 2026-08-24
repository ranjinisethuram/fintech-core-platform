package com.fintech.ledger.handler;

import com.fintech.common.command.CreateWalletLedgerAccountCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.ledger.repository.ProcessedMessagesRepository;
import com.fintech.ledger.service.LedgerService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class CreateWalletLedgerAccountHandler implements MessageHandler {

    private final LedgerService ledgerService;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public CreateWalletLedgerAccountHandler(LedgerService ledgerService, ProcessedMessagesRepository processedMessagesRepository) {
        this.ledgerService = ledgerService;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "CreateWalletLedgerAccountCommand";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0) {
            CreateWalletLedgerAccountCommand createWalletLedgerAccountCommand =
                    (CreateWalletLedgerAccountCommand) envelope.getPayload();
            this.ledgerService.createCustomerLedgerAccount(createWalletLedgerAccountCommand,
                    envelope.getCorrelationId(),
                    envelope.getMessageId(),
                    envelope.getSagaId());
        }
    }
}
