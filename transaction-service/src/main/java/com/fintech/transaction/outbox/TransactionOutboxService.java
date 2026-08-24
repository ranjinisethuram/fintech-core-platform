package com.fintech.transaction.outbox;

import com.fintech.outbox.OutboxStatus;
import com.fintech.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionOutboxService {

    private final TransactionOutboxRepository transactionOutboxRepository;

    public TransactionOutboxService(TransactionOutboxRepository transactionOutboxRepository) {
        this.transactionOutboxRepository = transactionOutboxRepository;
    }

    @Value("${outbox.poller.batch-size:50}")
    private int batchSize;

    @Transactional
    protected List<TransactionOutboxEvent> fetchBatchAndMarkInProgress() {
        List<TransactionOutboxEvent> transactionOutboxEvents =  transactionOutboxRepository
                .fetchProcessableBatch(batchSize);
        for (TransactionOutboxEvent event : transactionOutboxEvents){
            event.markInprogress();
        }
        return transactionOutboxRepository.saveAll(transactionOutboxEvents);
    }

    @Transactional
    protected void markSuccess(TransactionOutboxEvent event) {
//        transactionOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.SUCCESS.toString(), Instant.now());
        event.markProcessed();
        transactionOutboxRepository.save(event);
    }

    @Transactional
    protected void markRetry(TransactionOutboxEvent event, int retryCount) {
        Duration backOff = exponentialBackoff(retryCount);
//        Instant now = Instant.now();
//        Instant retryAt = now.plus(backOff);
//        transactionOutboxRepository.updateRetryStatus(eventId,
//                OutboxStatus.RETRY.toString(), retryAt, now);
        event.markRetry(backOff);
        transactionOutboxRepository.save(event);
    }

    //    @Override
//    @Transactional
//    protected void markInProgress(CustomerOutboxEvent event) {
//        customerOutboxRepository.findById(event.getId())
//                .ifPresent(CustomerOutboxEvent::markInprogress);
//    }

    @Transactional
    protected void markFailed(TransactionOutboxEvent event) {
//        transactionOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.FAILED.toString(), Instant.now());
        event.markFailed();
        transactionOutboxRepository.save(event);
    }

    private Duration exponentialBackoff(int retry) {
        return Duration.ofSeconds((long) Math.pow(2, retry));
    }
}
