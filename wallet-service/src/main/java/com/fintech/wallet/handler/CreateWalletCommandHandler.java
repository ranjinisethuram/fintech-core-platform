package com.fintech.wallet.handler;

import com.fintech.common.command.CreateWalletCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.wallet.repository.ProcessedMessagesRepository;
import com.fintech.wallet.service.WalletService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class CreateWalletCommandHandler implements MessageHandler {
    
    private final WalletService walletService;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public CreateWalletCommandHandler(WalletService walletService, ProcessedMessagesRepository processedMessagesRepository) {
        this.walletService = walletService;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "CreateWalletCommand";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            CreateWalletCommand createWalletCommand = (CreateWalletCommand) envelope.getPayload();
            this.walletService.createWallet(createWalletCommand,envelope.getCorrelationId()
                    ,envelope.getMessageId(), envelope.getSagaId());
        }
    }
}
