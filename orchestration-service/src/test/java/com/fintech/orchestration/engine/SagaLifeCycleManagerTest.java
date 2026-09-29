package com.fintech.orchestration.engine;

import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.StepId;
import com.fintech.orchestration.service.SagaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SagaLifeCycleManagerTest {

    @Mock
    private SagaService sagaService;

    @Mock
    private SagaStepRegistry sagaStepRegistry;

    private SagaLifeCycleManager sagaLifeCycleManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sagaLifeCycleManager = new SagaLifeCycleManager(sagaService, sagaStepRegistry);
    }

    @Test
    void fail_whenRetryable_shouldMarkSagaForRetry() {
        Saga saga = Saga.start("corr-1", "agg-1");
        saga.setCompletedSteps(List.of(StepId.FRAUD_CHECK, StepId.RESERVE_FUNDS));

        MessageEnvelope<?> envelope = mock(MessageEnvelope.class);
        when(envelope.getSagaId()).thenReturn(saga.getSagaId().toString());
        when(sagaService.fetchSagaById(saga.getSagaId())).thenReturn(saga);
        when(sagaService.isSagaRetryable(saga.getRetryCount())).thenReturn(true);

        sagaLifeCycleManager.fail(envelope, true, "temporary failure");

        verify(sagaService).updateSagaForRetry(saga);
        verify(sagaService, never()).updateSagaFailed(any(), any());
    }

    @Test
    void fail_whenNonRetryableAndCompensationConfigured_shouldExecuteFirstCompensationStep() {
        Saga saga = Saga.start("corr-2", "agg-2");
        saga.setCompletedSteps(List.of(StepId.FRAUD_CHECK, StepId.RESERVE_FUNDS, StepId.CREATE_LEDGER_ENTRIES));

        MessageEnvelope<?> envelope = mock(MessageEnvelope.class);
        when(envelope.getSagaId()).thenReturn(saga.getSagaId().toString());
        when(envelope.getMessageId()).thenReturn("message-1");
        when(sagaService.fetchSagaById(saga.getSagaId())).thenReturn(saga);
        when(sagaService.isSagaRetryable(saga.getRetryCount())).thenReturn(false);
        when(sagaService.updateSagaFailed(saga, "hard failure")).thenReturn(saga);

        SagaStep reserveStep = mock(SagaStep.class);
        SagaStep ledgerStep = mock(SagaStep.class);
        when(sagaStepRegistry.getStep(StepId.RESERVE_FUNDS)).thenReturn(reserveStep);
        when(reserveStep.compensationStep()).thenReturn(Optional.of(StepId.COMPENSATE_FUNDS));
        when(sagaStepRegistry.getStep(StepId.CREATE_LEDGER_ENTRIES)).thenReturn(ledgerStep);
        when(ledgerStep.compensationStep()).thenReturn(Optional.of(StepId.REVERSE_LEDGER_ENTRIES));

        SagaStep reverseStep = mock(SagaStep.class);
        when(sagaStepRegistry.getStep(StepId.REVERSE_LEDGER_ENTRIES)).thenReturn(reverseStep);

        sagaLifeCycleManager.fail(envelope, false, "hard failure");

        verify(sagaService).updateSagaFailed(saga, "hard failure");
        verify(sagaService).updateSagaForCompensation(saga);
        verify(reverseStep).execute(eq(saga), eq("message-1"));
    }

    @Test
    void compensationFailed_whenRetryable_shouldMarkSagaForRetry() {
        Saga saga = Saga.start("corr-3", "agg-3");
        MessageEnvelope<?> envelope = mock(MessageEnvelope.class);
        when(envelope.getSagaId()).thenReturn(saga.getSagaId().toString());
        when(sagaService.fetchSagaById(saga.getSagaId())).thenReturn(saga);
        when(sagaService.isSagaRetryable(saga.getRetryCount())).thenReturn(true);

        sagaLifeCycleManager.compensationFailed(envelope, true, "compensation retryable failure");

        verify(sagaService).updateSagaForRetry(saga);
        verify(sagaService, never()).updateSagaCompensationFailed(any(), any());
    }
}
