package com.fintech.wallet.handler;

import com.fintech.common.command.CommitWalletFundCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.wallet.repository.ProcessedMessagesRepository;
import com.fintech.wallet.service.WalletService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class CommitWalletFundCommandHandler implements MessageHandler {

    private final WalletService walletService;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public CommitWalletFundCommandHandler(WalletService walletService
            , ProcessedMessagesRepository processedMessagesRepository) {
        this.walletService = walletService;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "CommitWalletFundCommand";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            CommitWalletFundCommand commitWalletFundCommand = (CommitWalletFundCommand) envelope.getPayload();
            this.walletService.commitFunds(commitWalletFundCommand,envelope.getCorrelationId()
                    ,envelope.getMessageId(), envelope.getSagaId());
        }
    }
}
