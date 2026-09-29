package com.fintech.ledger.outbox;

import com.fintech.common.messaging.KafkaEventProducer;
import com.fintech.outbox.OutboxPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class LedgerOutboxPoller extends OutboxPoller<LedgerOutboxEvent> {

    private final LedgerOutboxService ledgerOutboxService;

    @Value("${outbox.poller.retry-limit:5}")
    private int retryLimit;

    public LedgerOutboxPoller(KafkaEventProducer kafkaEventProducer
            , LedgerOutboxService ledgerOutboxService) {
        super(kafkaEventProducer);
        this.ledgerOutboxService = ledgerOutboxService;
    }

    @Scheduled(fixedDelayString = "${outbox.poller.fixed-delay-ms:5000}")
    public void poll() {
        System.out.println("Scheduler running at " + Instant.now());
        pollInternal();
    }

    @Override
    protected List<LedgerOutboxEvent> fetchBatchAndMarkInProgress() {
        return this.ledgerOutboxService.fetchBatchAndMarkInProgress();
    }

    @Override
    protected String resolveTopic(LedgerOutboxEvent event) {
        //return event.getAggregateType().toLowerCase() + "-events";
        return "ledger-events";
    }

    @Override
    protected void markSuccess(LedgerOutboxEvent event) {
        this.ledgerOutboxService.markSuccess(event);
    }

    @Override
    protected void markRetry(LedgerOutboxEvent event, int retryCount) {
        this.ledgerOutboxService.markRetry(event,retryCount);
    }

    @Override
    protected void markFailed(LedgerOutboxEvent event) {
        this.ledgerOutboxService.markFailed(event);
    }

    @Override
    protected int getRetryLimit() {
        return retryLimit;
    }
}
