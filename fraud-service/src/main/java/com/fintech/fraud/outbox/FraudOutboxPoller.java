package com.fintech.fraud.outbox;

import com.fintech.common.messaging.KafkaEventProducer;
import com.fintech.outbox.OutboxPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.List;

@Component
public class FraudOutboxPoller extends OutboxPoller<FraudOutboxEvent> {

    private final FraudOutboxService fraudOutboxService;

    @Value("${outbox.poller.retry-limit:5}")
    private int retryLimit;

    public FraudOutboxPoller(FraudOutboxService fraudOutboxService,
                           KafkaEventProducer kafkaEventProducer) {
        super(kafkaEventProducer);
        this.fraudOutboxService = fraudOutboxService;
    }

    @Scheduled(fixedDelayString = "${outbox.poller.fixed-delay-ms:5000}")
    public void poll() {
        System.out.println("Fraud Outbox Scheduler running at " + Instant.now());
        pollInternal();
    }

    @Override
    protected List<FraudOutboxEvent> fetchBatchAndMarkInProgress() {
        return this.fraudOutboxService.fetchBatchAndMarkInProgress();
    }

    @Override
    protected String resolveTopic(FraudOutboxEvent event) {
        //return event.getAggregateType().toLowerCase() + "-events";
        return "fraud-events";
    }

    @Override
    protected void markSuccess(FraudOutboxEvent event) {
        this.fraudOutboxService.markSuccess(event);
    }

    @Override
    protected void markRetry(FraudOutboxEvent event, int retryCount) {
        this.fraudOutboxService.markRetry(event, retryCount);
    }

    @Override
    protected void markFailed(FraudOutboxEvent event) {
        this.fraudOutboxService.markFailed(event);
    }

    @Override
    protected int getRetryLimit() {
        return retryLimit;
    }
}
