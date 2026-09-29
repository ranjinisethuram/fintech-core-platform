package com.fintech.orchestration.sagastep.transactionhandling;

import com.fintech.common.command.EvaluateFraudCommand;
import com.fintech.fraudcontract.dto.HistoricalTransaction;
import com.fintech.common.orchestration.contextmapper.SagaContextMapper;
import com.fintech.common.orchestration.contextmapper.TransactionHandlingContext;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.SagaStep;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EvaluateFraudStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public EvaluateFraudStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.FRAUD_CHECK;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishEvaluateFraudCommandToOutbox(saga, messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishEvaluateFraudCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishEvaluateFraudCommandToOutbox(Saga currentSaga,
                                                     String messageId,
                                                     UUID sagaRecoveryId,
                                                     boolean forRecover) {
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        TransactionHandlingContext transactionHandlingContext =
                this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                        TransactionHandlingContext.class);
        List<HistoricalTransaction> historicalTransactionList = sagaService.
                fetchLast30DaysTransactionHistory(
                        transactionHandlingContext.getSourceAccountId());
        List<EvaluateFraudCommand.HistoricalTransaction> transactionHistoryList =
                historicalTransactionList.stream()
                        .map(transactionHistory
                                -> new EvaluateFraudCommand.HistoricalTransaction(
                                        transactionHistory.amount(),
                                        transactionHistory.timestamp()))
                        .collect(Collectors.toList());
        EvaluateFraudCommand command = new EvaluateFraudCommand(
                transactionHandlingContext.getTransactionId(),
                transactionHandlingContext.getAmount(),
                Instant.now(),
                transactionHistoryList
        );

        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(command,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "fraud-commands");
    }
}
