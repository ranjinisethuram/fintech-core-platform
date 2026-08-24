package com.fintech.customer.outbox;

import com.fintech.outbox.OutboxStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class CustomerOutboxService {

    private final CustomerOutboxRepository customerOutboxRepository;

    public CustomerOutboxService(CustomerOutboxRepository customerOutboxRepository) {
        this.customerOutboxRepository = customerOutboxRepository;
    }

    @Value("${outbox.poller.batch-size:50}")
    private int batchSize;

    @Transactional
    protected List<CustomerOutboxEvent> fetchBatchAndMarkInProgress() {
        List<CustomerOutboxEvent> customerOutboxEvents =  customerOutboxRepository
                .fetchProcessableBatch(batchSize);
        for (CustomerOutboxEvent event : customerOutboxEvents){
            event.markInprogress();
        }
        return customerOutboxRepository.saveAll(customerOutboxEvents);
    }

    @Transactional
    protected void markSuccess(CustomerOutboxEvent event) {
//        customerOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.SUCCESS.toString(), Instant.now());
        event.markProcessed();
        customerOutboxRepository.save(event);
    }

    @Transactional
    protected void markRetry(CustomerOutboxEvent event, int retryCount) {
        Duration backOff = exponentialBackoff(retryCount);
//        Instant now = Instant.now();
//        Instant retryAt = now.plus(backOff);
//        customerOutboxRepository.updateRetryStatus(eventId,
//                OutboxStatus.RETRY.toString(), retryCount, retryAt, now);
        event.markRetry(backOff);
        customerOutboxRepository.save(event);
    }

    @Transactional
    protected void markFailed(CustomerOutboxEvent event) {
//        customerOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.FAILED.toString(), Instant.now());
        event.markFailed();
        customerOutboxRepository.save(event);
    }

    private Duration exponentialBackoff(int retry) {
        return Duration.ofSeconds((long) Math.pow(2, retry));
    }
}
