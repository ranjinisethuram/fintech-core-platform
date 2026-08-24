package com.fintech.orchestration.sagastep.customeronboarding;

import com.fintech.common.command.CreateWalletLedgerAccountCommand;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.orchestration.contextmapper.CustomerOnboardingContext;
import com.fintech.orchestration.contextmapper.SagaContextMapper;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.SagaStep;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateLedgerAccountStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public CreateLedgerAccountStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.CREATE_LEDGER_ACCOUNT;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishCreateLedgerAccountCommandToOutbox(saga,messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishCreateLedgerAccountCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishCreateLedgerAccountCommandToOutbox(Saga currentSaga,
                      String messageId,
                      UUID sagaRecoveryId, boolean forRecover){
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        CustomerOnboardingContext customerOnboardingContext = this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                CustomerOnboardingContext.class);
        UUID ledgerAccountId = UUID.randomUUID();
        CreateWalletLedgerAccountCommand createWalletLedgerAccountCommand = new CreateWalletLedgerAccountCommand(
                ledgerAccountId,
                customerOnboardingContext.getWalletId(),
                customerOnboardingContext.getAccountId(),
                customerOnboardingContext.getCustomerId(),
                customerOnboardingContext.getCurrency()
        );
        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(createWalletLedgerAccountCommand,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "ledger-commands",
                false);
    }
}
