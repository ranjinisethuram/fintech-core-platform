package com.fintech.wallet.handler;

import com.fintech.common.command.CreditWalletCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageHandler;
import com.fintech.wallet.repository.ProcessedMessagesRepository;
import com.fintech.wallet.service.WalletService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class CreditWalletCommandHandler implements MessageHandler {

    private final WalletService walletService;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public CreditWalletCommandHandler(WalletService walletService, ProcessedMessagesRepository processedMessagesRepository) {
        this.walletService = walletService;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Override
    public String eventType() {
        return "CreditWalletCommand";
    }

    @Override
    @Transactional
    public void handle(MessageEnvelope<?> envelope) {
        int rowsInserted = processedMessagesRepository.insert(envelope.getMessageId(), Instant.now());
        if(rowsInserted != 0){
            CreditWalletCommand creditWalletCommand = (CreditWalletCommand) envelope.getPayload();
            this.walletService.creditWallet(creditWalletCommand,envelope.getCorrelationId()
                    ,envelope.getMessageId(), envelope.getSagaId());
        }
    }
}
