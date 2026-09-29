package com.fintech.orchestration.sagastep.transactionhandling;

import com.fintech.common.command.ReverseLedgerEntryCommand;
import com.fintech.common.orchestration.contextmapper.SagaContextMapper;
import com.fintech.common.orchestration.contextmapper.TransactionHandlingContext;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.SagaStep;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReverseLedgerEntriesStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public ReverseLedgerEntriesStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.REVERSE_LEDGER_ENTRIES;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishReverseLedgerEntriesCommandToOutbox(saga, messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishReverseLedgerEntriesCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishReverseLedgerEntriesCommandToOutbox(Saga currentSaga,
                    String messageId,
                    UUID sagaRecoveryId, boolean forRecover){
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        TransactionHandlingContext transactionHandlingContext =
                this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                        TransactionHandlingContext.class);
        ReverseLedgerEntryCommand reverseLedgerEntryCommand = new ReverseLedgerEntryCommand(
                transactionHandlingContext.getTransactionId(),
                transactionHandlingContext.getSourceAccountId(),
                transactionHandlingContext.getDestinationAccountId(),
                transactionHandlingContext.getAmount(),
                transactionHandlingContext.getCurrency(),
                transactionHandlingContext.getTransactionType()
        );
        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(reverseLedgerEntryCommand,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "ledger-commands");
    }
}
