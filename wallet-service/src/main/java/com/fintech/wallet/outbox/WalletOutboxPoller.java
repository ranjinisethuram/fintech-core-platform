package com.fintech.wallet.outbox;

import com.fintech.common.messaging.KafkaEventProducer;
import com.fintech.outbox.OutboxPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class WalletOutboxPoller extends OutboxPoller<WalletOutboxEvent> {

    private final WalletOutboxService walletOutboxService;

    @Value("${outbox.poller.retry-limit:5}")
    private int retryLimit;

    public WalletOutboxPoller(WalletOutboxService walletOutboxService
            , KafkaEventProducer kafkaEventProducer) {
        super(kafkaEventProducer);
        this.walletOutboxService = walletOutboxService;
    }

    @Scheduled(fixedDelayString = "${outbox.poller.fixed-delay-ms:5000}")
    public void poll() {
        System.out.println("Scheduler running at " + Instant.now());
        pollInternal();
    }

    @Override
    protected List<WalletOutboxEvent> fetchBatchAndMarkInProgress() {
        return this.walletOutboxService.fetchBatchAndMarkInProgress();
    }

    @Override
    protected String resolveTopic(WalletOutboxEvent event) {
        //return event.getAggregateType().toLowerCase() + "-events";
        return "waller-events";
    }

    @Override
    protected void markSuccess(WalletOutboxEvent event) {
        this.walletOutboxService.markSuccess(event);
    }

    @Override
    protected void markRetry(WalletOutboxEvent event, int retryCount) {
        this.walletOutboxService.markRetry(event,retryCount);
    }

    @Override
    protected void markFailed(WalletOutboxEvent event) {
        this.walletOutboxService.markFailed(event);
    }

    @Override
    protected int getRetryLimit() {
        return retryLimit;
    }
}
