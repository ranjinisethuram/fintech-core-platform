package com.fintech.account.handler;

import com.fintech.account.repository.ProcessedMessagesRepository;
import com.fintech.account.service.AccountService;
import com.fintech.common.command.ActivateAccountCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class ActivateCommandHandler implements MessageHandler {

    private final AccountService accountService;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public ActivateCommandHandler(AccountService accountService, ProcessedMessagesRepository processedMessagesRepository) {
        this.accountService = accountService;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "ActivateAccountCommand";
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0) {
            ActivateAccountCommand activateAccountCommand =
                    (ActivateAccountCommand) envelope.getPayload();
            this.accountService.activateAccount(activateAccountCommand, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        }
    }
}
