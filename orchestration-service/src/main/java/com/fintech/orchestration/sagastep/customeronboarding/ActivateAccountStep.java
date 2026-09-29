package com.fintech.orchestration.sagastep.customeronboarding;

import com.fintech.common.command.ActivateAccountCommand;
import com.fintech.common.orchestration.contextmapper.CustomerOnboardingContext;
import com.fintech.common.orchestration.contextmapper.SagaContextMapper;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.SagaStep;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ActivateAccountStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public ActivateAccountStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.ACTIVATE_ACCOUNT;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishActivateAccountCommandToOutbox(saga,messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishActivateAccountCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishActivateAccountCommandToOutbox(Saga currentSaga,
                   String messageId,
                   UUID sagaRecoveryId, boolean forRecover){
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        CustomerOnboardingContext customerOnboardingContext = this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                CustomerOnboardingContext.class);
        ActivateAccountCommand activateAccountCommand = new ActivateAccountCommand(
                customerOnboardingContext.getAccountId(),
                customerOnboardingContext.getCustomerId()
        );
        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(activateAccountCommand,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "account-commands");
    }
}
