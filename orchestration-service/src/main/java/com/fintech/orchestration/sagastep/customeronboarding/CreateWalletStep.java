package com.fintech.orchestration.sagastep.customeronboarding;

import com.fintech.common.command.CreateWalletCommand;
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
public class CreateWalletStep implements SagaStep {

    private final SagaService sagaService;
    private final SagaContextMapper sagaContextMapper;

    public CreateWalletStep(SagaService sagaService, SagaContextMapper sagaContextMapper) {
        this.sagaService = sagaService;
        this.sagaContextMapper = sagaContextMapper;
    }

    @Override
    public StepId stepId() {
        return StepId.CREATE_WALLET;
    }

    @Override
    public void execute(Saga saga, String messageId) {
        publishCreateWalletCommandToOutbox(saga, messageId, null, false);
    }

    @Override
    public void recover(Saga saga, UUID sagaRecoveryId) {
        publishCreateWalletCommandToOutbox(saga, null, sagaRecoveryId, true);
    }

    private void publishCreateWalletCommandToOutbox(Saga currentSaga,
                    String messageId,
                    UUID sagaRecoveryId, boolean forRecover) {
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(currentSaga.getSagaId());
        CustomerOnboardingContext customerOnboardingContext = this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
                CustomerOnboardingContext.class);
        UUID walletId = UUID.randomUUID();
        CreateWalletCommand createWalletCommand = new CreateWalletCommand(
                walletId,
                customerOnboardingContext.getAccountId(),
                customerOnboardingContext.getCustomerId()
        );
        String causationId = forRecover ? sagaRecoveryId.toString() : messageId;
        this.sagaService.buildMessageEnvelopeAndPushToOutbox(createWalletCommand,
                currentSaga.getCorrelationId(),
                causationId,
                currentSaga.getSagaId().toString(),
                "wallet-commands");
    }
}
