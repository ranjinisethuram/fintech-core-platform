package com.fintech.ledger.outbox;

import com.fintech.outbox.OutboxStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class LedgerOutboxService {

    private final LedgerOutboxRepository ledgerOutboxRepository;

    public LedgerOutboxService(LedgerOutboxRepository ledgerOutboxRepository) {
        this.ledgerOutboxRepository = ledgerOutboxRepository;
    }

    @Value("${outbox.poller.batch-size:50}")
    private int batchSize;

    @Transactional
    protected List<LedgerOutboxEvent> fetchBatchAndMarkInProgress() {
        List<LedgerOutboxEvent> ledgerOutboxEvents =  ledgerOutboxRepository
                .fetchProcessableBatch(batchSize);
        for (LedgerOutboxEvent event : ledgerOutboxEvents){
            event.markInprogress();
        }
        return ledgerOutboxRepository.saveAll(ledgerOutboxEvents);
    }

    @Transactional
    protected void markSuccess(LedgerOutboxEvent event) {
//        ledgerOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.SUCCESS.toString(), Instant.now());
        event.markProcessed();
        ledgerOutboxRepository.save(event);
    }

    @Transactional
    protected void markRetry(LedgerOutboxEvent event, int retryCount) {
        Duration backOff = exponentialBackoff(retryCount);
//        Instant now = Instant.now();
//        Instant retryAt = now.plus(backOff);
//        ledgerOutboxRepository.updateRetryStatus(eventId,
//                OutboxStatus.RETRY.toString(), retryAt, now);
        event.markRetry(backOff);
        ledgerOutboxRepository.save(event);
    }

    //    @Override
//    @Transactional
//    protected void markInProgress(CustomerOutboxEvent event) {
//        customerOutboxRepository.findById(event.getId())
//                .ifPresent(CustomerOutboxEvent::markInprogress);
//    }

    @Transactional
    protected void markFailed(LedgerOutboxEvent event) {
//        ledgerOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.FAILED.toString(), Instant.now());
        event.markFailed();
        ledgerOutboxRepository.save(event);
    }

    private Duration exponentialBackoff(int retry) {
        return Duration.ofSeconds((long) Math.pow(2, retry));
    }
}
