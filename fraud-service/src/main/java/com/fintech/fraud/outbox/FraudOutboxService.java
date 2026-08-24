package com.fintech.fraud.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class FraudOutboxService {

    private final FraudOutboxRepository fraudOutboxRepository;

    public FraudOutboxService(FraudOutboxRepository fraudOutboxRepository) {
        this.fraudOutboxRepository = fraudOutboxRepository;
    }

    @Value("${outbox.poller.batch-size:50}")
    private int batchSize;

    @Transactional
    public List<FraudOutboxEvent> fetchBatchAndMarkInProgress() {
        List<FraudOutboxEvent> fraudOutboxEvents = fraudOutboxRepository
                .fetchProcessableBatch(batchSize);
        for (FraudOutboxEvent event : fraudOutboxEvents) {
            event.markInprogress();
        }
        return fraudOutboxRepository.saveAll(fraudOutboxEvents);
    }

    @Transactional
    public void markSuccess(FraudOutboxEvent event) {
        event.markProcessed();
        fraudOutboxRepository.save(event);
    }

    @Transactional
    public void markRetry(FraudOutboxEvent event, int retryCount) {
        Duration backOff = exponentialBackoff(retryCount);
        event.markRetry(backOff);
        fraudOutboxRepository.save(event);
    }

    @Transactional
    public void markFailed(FraudOutboxEvent event) {
        event.markFailed();
        fraudOutboxRepository.save(event);
    }

    private Duration exponentialBackoff(int retry) {
        return Duration.ofSeconds((long) Math.pow(2, retry));
    }
}
