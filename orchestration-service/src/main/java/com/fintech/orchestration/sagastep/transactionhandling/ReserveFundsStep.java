package com.fintech.orchestration.sagastep.transactionhandling;

import com.fintech.common.command.ReserveWalletFundCommand;
import com.fintech.common.orchestration.contextmapper.SagaContextMapper;
import com.fintech.common.orchestration.contextmapper.TransactionHandlingContext;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.SagaStep;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ReserveFundsStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public ReserveFundsStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.RESERVE_FUNDS;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishReserveFundsCommandToOutbox(saga, messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishReserveFundsCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    @Override
    public Optional<StepId> compensationStep() {
        return Optional.of(StepId.COMPENSATE_FUNDS);
    }

    private void publishReserveFundsCommandToOutbox(Saga currentSaga,
                                                    String messageId,
                                                    UUID sagaRecoveryId, boolean forRecover) {
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        TransactionHandlingContext transactionHandlingContext =
                this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                        TransactionHandlingContext.class);
        ReserveWalletFundCommand reserveWalletFundCommand = new ReserveWalletFundCommand(
                transactionHandlingContext.getSourceAccountId(),
                transactionHandlingContext.getTransactionId(),
                transactionHandlingContext.getAmount(),
                transactionHandlingContext.getCurrency(),
                transactionHandlingContext.getTransactionType());
        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(reserveWalletFundCommand,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "wallet-commands");
    }
}
