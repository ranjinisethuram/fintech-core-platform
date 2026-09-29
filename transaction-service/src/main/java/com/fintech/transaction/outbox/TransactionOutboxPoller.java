package com.fintech.transaction.outbox;

import com.fintech.common.messaging.KafkaEventProducer;
import com.fintech.outbox.OutboxPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionOutboxPoller extends OutboxPoller<TransactionOutboxEvent> {

    private final TransactionOutboxService transactionOutboxService;

    public TransactionOutboxPoller(TransactionOutboxService transactionOutboxService
            , KafkaEventProducer kafkaEventProducer) {
        super(kafkaEventProducer);
        this.transactionOutboxService = transactionOutboxService;
    }

    @Value("${outbox.poller.retry-limit:5}")
    private int retryLimit;

    @Scheduled(fixedDelayString = "${outbox.poller.fixed-delay-ms:5000}")
    public void poll() {
        System.out.println("Scheduler running at " + Instant.now());
        pollInternal();
    }

    @Override
    protected List<TransactionOutboxEvent> fetchBatchAndMarkInProgress() {
        return transactionOutboxService.fetchBatchAndMarkInProgress();
    }

    @Override
    protected String resolveTopic(TransactionOutboxEvent event) {
        //return event.getAggregateType().toLowerCase() + "-events";
        return "transaction-events";
    }

    @Override
    protected void markSuccess(TransactionOutboxEvent event) {
        transactionOutboxService.markSuccess(event);
    }

    @Override
    protected void markRetry(TransactionOutboxEvent event, int retryCount) {
        transactionOutboxService.markRetry(event, retryCount);
    }

    @Override
    protected void markFailed(TransactionOutboxEvent event) {
        transactionOutboxService.markFailed(event);
    }

    @Override
    protected int getRetryLimit() {
        return retryLimit;
    }
}
