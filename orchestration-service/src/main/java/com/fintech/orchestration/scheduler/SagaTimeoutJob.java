package com.fintech.orchestration.scheduler;

import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.repository.SagaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class SagaTimeoutJob {

    private final SagaRepository sagaRepository;

    @Value("${saga.timeout.batch-size:50}")
    private int batchSize;

    public SagaTimeoutJob(SagaRepository sagaRepository) {
        this.sagaRepository = sagaRepository;
    }

    @Scheduled(fixedDelayString = "${saga.timeout.fixed-delay-ms:60000}")
    @Transactional
    public void fetchStuckSagas(){
        List<Saga> stuckSagas = this.sagaRepository.fetchStuckSagas(batchSize);
        for(Saga saga : stuckSagas){
            saga.timeout();
        }
        this.sagaRepository.saveAll(stuckSagas);
    }

}
