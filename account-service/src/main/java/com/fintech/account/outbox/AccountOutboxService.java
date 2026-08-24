package com.fintech.account.outbox;

import com.fintech.outbox.OutboxStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class AccountOutboxService {

    private final AccountOutboxRepository accountOutboxRepository;

    public AccountOutboxService(AccountOutboxRepository accountOutboxRepository) {
        this.accountOutboxRepository = accountOutboxRepository;
    }

    @Value("${outbox.poller.batch-size:50}")
    private int batchSize;

    @Transactional
    protected List<AccountOutboxEvent> fetchBatchAndMarkInProgress() {
        List<AccountOutboxEvent> accountOutboxEvents =  accountOutboxRepository
                .fetchProcessableBatch(batchSize);
        for (AccountOutboxEvent event : accountOutboxEvents){
            event.markInprogress();
        }
        return accountOutboxRepository.saveAll(accountOutboxEvents);
    }

    @Transactional
    protected void markSuccess(AccountOutboxEvent event) {
        /*accountOutboxRepository.updateProcessedStatus(eventId,
                OutboxStatus.SUCCESS.toString(), Instant.now());*/
        event.markProcessed();
        this.accountOutboxRepository.save(event);
    }

    @Transactional
    protected void markRetry(AccountOutboxEvent event, int retryCount) {
        Duration backOff = exponentialBackoff(retryCount);
        event.markRetry(backOff);
        this.accountOutboxRepository.save(event);
//        Instant now = Instant.now();
//        Instant retryAt = now.plus(backOff);
//        accountOutboxRepository.updateRetryStatus(eventId,
//                OutboxStatus.RETRY.toString(), retryAt, now);
    }

    @Transactional
    protected void markFailed(AccountOutboxEvent event) {
//        accountOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.FAILED.toString(), Instant.now());
        event.markFailed();
        accountOutboxRepository.save(event);
    }

    private Duration exponentialBackoff(int retry) {
        return Duration.ofSeconds((long) Math.pow(2, retry));
    }
}
