package com.fintech.orchestration.outbox;

import com.fintech.common.messaging.KafkaEventProducer;
import com.fintech.outbox.OutboxPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class SagaOutboxPoller extends OutboxPoller<SagaOutbox> {

    private final SagaOutboxService sagaOutboxService;

    @Value("${outbox.poller.retry-limit:5}")
    private int retryLimit;

    public SagaOutboxPoller(SagaOutboxService sagaOutboxService
            , KafkaEventProducer kafkaEventProducer) {
        super(kafkaEventProducer);
        this.sagaOutboxService = sagaOutboxService;
    }

    @Scheduled(fixedDelayString = "${outbox.poller.fixed-delay-ms:5000}")
    public void poll() {
        System.out.println("Scheduler running at " + Instant.now());
        pollInternal();
    }

    @Override
    protected List<SagaOutbox> fetchBatchAndMarkInProgress() {
        return sagaOutboxService.fetchBatchAndMarkInProgress();
    }

    @Override
    protected String resolveTopic(SagaOutbox event) {
        return sagaOutboxService.getTopicName(event.getId());
    }

    @Override
    protected void markSuccess(SagaOutbox event) {
        sagaOutboxService.markSuccess(event);
    }

    @Override
    protected void markRetry(SagaOutbox event, int retryCount) {
        sagaOutboxService.markRetry(event, retryCount);
    }

    @Override
    protected void markFailed(SagaOutbox event) {
        sagaOutboxService.markFailed(event);
    }

    @Override
    public int getRetryLimit() {
        return retryLimit;
    }
}
