package com.fintech.customer.outbox;


import com.fintech.common.messaging.KafkaEventProducer;
import com.fintech.outbox.OutboxPoller;
import com.fintech.outbox.OutboxStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class CustomerOutboxPoller extends OutboxPoller<CustomerOutboxEvent>{

    private final CustomerOutboxService customerOutboxService;

    @Value("${outbox.poller.retry-limit:5}")
    private int retryLimit;

    public CustomerOutboxPoller(CustomerOutboxService customerOutboxService
            , KafkaEventProducer kafkaEventProducer) {
        super(kafkaEventProducer);
        this.customerOutboxService = customerOutboxService;
    }

    @Scheduled(fixedDelayString = "${outbox.poller.fixed-delay-ms:5000}")
    public void poll() {
        System.out.println("Scheduler running at " + Instant.now());
        pollInternal();
    }

    @Override
    protected List<CustomerOutboxEvent> fetchBatchAndMarkInProgress() {
        return customerOutboxService.fetchBatchAndMarkInProgress();
    }

    @Override
    protected String resolveTopic(CustomerOutboxEvent customerOutboxEvent) {
        //return event.getAggregateType().toLowerCase() + "-events";
        return "customer-events";
    }

    @Override
    protected void markSuccess(CustomerOutboxEvent event) {
        customerOutboxService.markSuccess(event);
    }

    @Override
    protected void markRetry(CustomerOutboxEvent event, int retryCount) {
        customerOutboxService.markRetry(event, retryCount);
    }

    @Override
    protected void markFailed(CustomerOutboxEvent event) {
        customerOutboxService.markFailed(event);
    }

    @Override
    public int getRetryLimit() {
        return retryLimit;
    }
}
