package com.fintech.orchestration.sagastep.transactionhandling;

import com.fintech.common.command.CreditWalletCommand;
import com.fintech.orchestration.contextmapper.SagaContextMapper;
import com.fintech.orchestration.contextmapper.TransactionHandlingContext;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.SagaStep;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreditWalletStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public CreditWalletStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.CREDIT_WALLET;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishCreditWalletCommandToOutbox(saga, messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishCreditWalletCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishCreditWalletCommandToOutbox(Saga currentSaga,
                                                    String messageId,
                                                    UUID sagaRecoveryId, boolean forRecover) {
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        TransactionHandlingContext transactionHandlingContext =
                this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                        TransactionHandlingContext.class);
        CreditWalletCommand creditWalletCommand = new CreditWalletCommand(
                transactionHandlingContext.getDestinationAccountId(),
                transactionHandlingContext.getTransactionId(),
                transactionHandlingContext.getAmount(),
                transactionHandlingContext.getCurrency(),
                transactionHandlingContext.getTransactionType());
        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(creditWalletCommand,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "wallet-commands",
                false);
    }
}
