package com.fintech.orchestration.scheduler;

import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaRecoveryAttempt;
import com.fintech.orchestration.repository.SagaRecoveryAttemptRepository;
import com.fintech.orchestration.repository.SagaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Component
public class SagaRetryJob {

    private final SagaRepository sagaRepository;
    private final SagaRecoveryService sagaRecoveryService;
    private final SagaRecoveryAttemptRepository sagaRecoveryAttemptRepository;

    @Value("${saga.retry.batch-size:50}")
    private int batchSize;

    public SagaRetryJob(SagaRepository sagaRepository, SagaRecoveryService sagaRecoveryService, SagaRecoveryAttemptRepository sagaRecoveryAttemptRepository) {
        this.sagaRepository = sagaRepository;
        this.sagaRecoveryService = sagaRecoveryService;
        this.sagaRecoveryAttemptRepository = sagaRecoveryAttemptRepository;
    }

    @Scheduled(fixedDelayString = "${saga.retry.fixed-delay-ms:60000}")
    @Transactional
    public void fetchRetryableSagas(){
        List<Saga> waitingSagaList = this.sagaRepository.fetchWaitingSagas(batchSize);
        for(Saga saga : waitingSagaList){
            Duration backoff = exponentialBackoff(saga.getRetryCount());
            saga.retry(backoff);
            this.sagaRepository.save(saga);
            SagaRecoveryAttempt sagaRecoveryAttempt = new SagaRecoveryAttempt(saga.getSagaId(), saga.getRetryCount());
            sagaRecoveryAttempt = this.sagaRecoveryAttemptRepository.save(sagaRecoveryAttempt);
            this.sagaRecoveryService.retryFailedSagaStep(saga, sagaRecoveryAttempt.getRecoveryId());
        }
    }

    private Duration exponentialBackoff(int retry) {
        return Duration.ofSeconds((long) Math.pow(2, retry));
    }
}
