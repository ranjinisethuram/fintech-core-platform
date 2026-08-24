package com.fintech.orchestration.sagastep.transactionhandling;

import com.fintech.common.command.CommitWalletFundCommand;
import com.fintech.common.command.ReserveWalletFundCommand;
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
public class CommitFundsStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public CommitFundsStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.COMMIT_FUNDS;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishCommitFundsCommandToOutbox(saga, messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishCommitFundsCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishCommitFundsCommandToOutbox(Saga currentSaga,
                                                    String messageId,
                                                    UUID sagaRecoveryId, boolean forRecover) {
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        TransactionHandlingContext transactionHandlingContext =
                this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                        TransactionHandlingContext.class);
        CommitWalletFundCommand commitWalletFundCommand = new CommitWalletFundCommand(
                transactionHandlingContext.getSourceAccountId(),
                transactionHandlingContext.getTransactionId(),
                transactionHandlingContext.getAmount(),
                transactionHandlingContext.getCurrency(),
                transactionHandlingContext.getTransactionType());
        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(commitWalletFundCommand,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "wallet-commands",
                false);
    }
}
