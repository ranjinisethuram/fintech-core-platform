package com.fintech.wallet.outbox;

import com.fintech.outbox.OutboxStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class WalletOutboxService {

    private final WalletOutboxRepository walletOutboxRepository;

    public WalletOutboxService(WalletOutboxRepository walletOutboxRepository) {
        this.walletOutboxRepository = walletOutboxRepository;
    }

    @Value("${outbox.poller.batch-size:50}")
    private int batchSize;

    @Transactional
    protected List<WalletOutboxEvent> fetchBatchAndMarkInProgress() {
        List<WalletOutboxEvent> walletOutboxEvents =  walletOutboxRepository
                .fetchProcessableBatch(batchSize);
        for (WalletOutboxEvent event : walletOutboxEvents){
            event.markInprogress();
        }
        return walletOutboxRepository.saveAll(walletOutboxEvents);
    }

    @Transactional
    protected void markSuccess(WalletOutboxEvent event) {
//        walletOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.SUCCESS.toString(), Instant.now());
        event.markProcessed();
        walletOutboxRepository.save(event);
    }

    @Transactional
    protected void markRetry(WalletOutboxEvent event, int retryCount) {
        Duration backOff = exponentialBackoff(retryCount);
//        Instant now = Instant.now();
//        Instant retryAt = now.plus(backOff);
//        walletOutboxRepository.updateRetryStatus(eventId,
//                OutboxStatus.RETRY.toString(), retryAt, now);
        event.markRetry(backOff);
        walletOutboxRepository.save(event);
    }

    //    @Override
//    @Transactional
//    protected void markInProgress(CustomerOutboxEvent event) {
//        customerOutboxRepository.findById(event.getId())
//                .ifPresent(CustomerOutboxEvent::markInprogress);
//    }

    @Transactional
    protected void markFailed(WalletOutboxEvent event) {
//        walletOutboxRepository.updateProcessedStatus(eventId,
//                OutboxStatus.FAILED.toString(), Instant.now());
        event.markFailed();
        walletOutboxRepository.save(event);
    }

    private Duration exponentialBackoff(int retry) {
        return Duration.ofSeconds((long) Math.pow(2, retry));
    }
}
