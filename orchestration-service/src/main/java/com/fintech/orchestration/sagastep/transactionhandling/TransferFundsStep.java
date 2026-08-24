package com.fintech.orchestration.sagastep.transactionhandling;

import com.fintech.common.command.CommitWalletFundCommand;
import com.fintech.common.command.TransferWalletAmountCommand;
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
public class TransferFundsStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public TransferFundsStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.FUND_TRANSFER;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishTransferFundsCommandToOutbox(saga, messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishTransferFundsCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishTransferFundsCommandToOutbox(Saga currentSaga,
                                                    String messageId,
                                                    UUID sagaRecoveryId, boolean forRecover) {
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        TransactionHandlingContext transactionHandlingContext =
                this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                        TransactionHandlingContext.class);
        TransferWalletAmountCommand transferWalletAmountCommand = new TransferWalletAmountCommand(
                transactionHandlingContext.getSourceAccountId(),
                transactionHandlingContext.getDestinationAccountId(),
                transactionHandlingContext.getTransactionId(),
                transactionHandlingContext.getAmount(),
                transactionHandlingContext.getCurrency(),
                transactionHandlingContext.getTransactionType());
        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(transferWalletAmountCommand,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "wallet-commands",
                false);
    }
}
