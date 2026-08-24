package com.fintech.account.handler;

import com.fintech.account.repository.ProcessedMessagesRepository;
import com.fintech.account.service.AccountService;
import com.fintech.common.command.CreateAccountCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class CreateAccountCommandHandler implements MessageHandler {

    private final AccountService accountService;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public CreateAccountCommandHandler(AccountService accountService, ProcessedMessagesRepository processedMessagesRepository) {
        this.accountService = accountService;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "CreateAccountCommand";
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0) {
            CreateAccountCommand createAccountCommand = (CreateAccountCommand) envelope.getPayload();
            accountService.createAccount(createAccountCommand, envelope.getCorrelationId(),
                    envelope.getMessageId(), envelope.getSagaId());
        }
    }
}
