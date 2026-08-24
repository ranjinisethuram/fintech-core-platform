package com.fintech.orchestration.outbox;

import com.fintech.outbox.OutboxStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class SagaOutboxService {

    private final SagaOutboxRepository sagaOutboxRepository;

    public SagaOutboxService(SagaOutboxRepository sagaOutboxRepository) {
        this.sagaOutboxRepository = sagaOutboxRepository;
    }

    @Value("${outbox.poller.batch-size:50}")
    private int batchSize;

    @Transactional
    protected List<SagaOutbox> fetchBatchAndMarkInProgress() {
        List<SagaOutbox> sagaOutboxEvents =  sagaOutboxRepository
                .fetchProcessableBatch(batchSize);
        for (SagaOutbox event : sagaOutboxEvents){
            event.markInprogress();
        }
        return sagaOutboxRepository.saveAll(sagaOutboxEvents);
    }

    @Transactional
    protected void markSuccess(SagaOutbox event) {
//        sagaOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.SUCCESS.toString(), Instant.now());
        event.markProcessed();
        sagaOutboxRepository.save(event);
    }

    @Transactional
    protected void markRetry(SagaOutbox event, int retryCount) {
        Duration backOff = exponentialBackoff(retryCount);
//        Instant now = Instant.now();
//        Instant retryAt = now.plus(backOff);
//        sagaOutboxRepository.updateRetryStatus(eventId,
//                OutboxStatus.RETRY.toString(), retryCount , retryAt, now);
        event.markRetry(backOff);
        sagaOutboxRepository.save(event);
    }

    //    @Override
//    @Transactional
//    protected void markInProgress(CustomerOutboxEvent event) {
//        customerOutboxRepository.findById(event.getId())
//                .ifPresent(CustomerOutboxEvent::markInprogress);
//    }

    @Transactional
    protected void markFailed(SagaOutbox event) {
//        sagaOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.FAILED.toString(), Instant.now());
        event.markFailed();
        sagaOutboxRepository.save(event);
    }

    @Transactional
    protected String getTopicName(UUID eventId) {
        return sagaOutboxRepository.getTopicNameById(eventId);
    }

    private Duration exponentialBackoff(int retry) {
        return Duration.ofSeconds((long) Math.pow(2, retry));
    }
}
