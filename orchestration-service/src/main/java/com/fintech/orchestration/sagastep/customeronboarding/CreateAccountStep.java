package com.fintech.orchestration.sagastep.customeronboarding;

import com.fintech.common.command.CreateAccountCommand;
import com.fintech.common.exception.BaseException;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.orchestration.contextmapper.CustomerOnboardingContext;
import com.fintech.orchestration.contextmapper.SagaContextMapper;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.engine.SagaStep;
import com.fintech.orchestration.exception.OrchestrationErrorCode;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateAccountStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public CreateAccountStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.CREATE_ACCOUNT;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        //this.sagaService.publishCreateAccountCommandToOutbox(saga, envelope);
        publishCreateAccountCommandToOutbox(saga,messageId,null,false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishCreateAccountCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishCreateAccountCommandToOutbox(Saga currentSaga,
                     String messageId,
                     UUID sagaRecoveryId, boolean forRecover) {
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        CustomerOnboardingContext customerOnboardingContext = this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                CustomerOnboardingContext.class);
        UUID accountId = UUID.randomUUID();
        CreateAccountCommand createAccountCommand = new CreateAccountCommand(accountId,
                customerOnboardingContext.getCustomerId(),customerOnboardingContext.isDefaultAccount());
        String caustaionId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(createAccountCommand,
                currentSaga.getCorrelationId(),
                caustaionId,
                currentSaga.getSagaId().toString(),
                "account-commands",
                false);
    }
}
