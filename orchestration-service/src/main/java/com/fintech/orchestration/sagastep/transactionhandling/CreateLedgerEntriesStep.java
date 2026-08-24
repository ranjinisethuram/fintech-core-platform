package com.fintech.orchestration.sagastep.transactionhandling;

import com.fintech.common.command.CreateLedgerEntryCommand;
import com.fintech.common.messaging.MessageEnvelope;
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
public class CreateLedgerEntriesStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public CreateLedgerEntriesStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.CREATE_LEDGER_ENTRIES;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishCreateLedgerEntriesCommandToOutbox(saga, messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishCreateLedgerEntriesCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishCreateLedgerEntriesCommandToOutbox(Saga currentSaga,
                    String messageId,
                    UUID sagaRecoveryId, boolean forRecover){
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        TransactionHandlingContext transactionHandlingContext =
                this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                        TransactionHandlingContext.class);
        CreateLedgerEntryCommand createLedgerEntryCommand = new CreateLedgerEntryCommand(
                transactionHandlingContext.getTransactionId(),
                transactionHandlingContext.getSourceAccountId(),
                transactionHandlingContext.getDestinationAccountId(),
                transactionHandlingContext.getAmount(),
                transactionHandlingContext.getCurrency(),
                transactionHandlingContext.getTransactionType()
        );
        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(createLedgerEntryCommand,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "ledger-commands",
                false);
    }
}
