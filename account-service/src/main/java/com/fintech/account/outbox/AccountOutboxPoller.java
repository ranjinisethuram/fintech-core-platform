package com.fintech.account.outbox;

import com.fintech.common.messaging.KafkaEventProducer;
import com.fintech.outbox.OutboxPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class AccountOutboxPoller extends OutboxPoller<AccountOutboxEvent> {

    private final AccountOutboxService accountOutboxService;

    @Value("${outbox.poller.retry-limit:5}")
    private int retryLimit;

    public AccountOutboxPoller(AccountOutboxService accountOutboxService
            , KafkaEventProducer kafkaEventProducer) {
        super(kafkaEventProducer);
        this.accountOutboxService = accountOutboxService;
    }

    @Scheduled(fixedDelayString = "${outbox.poller.fixed-delay-ms:5000}")
    public void poll() {
        System.out.println("Scheduler running at " + Instant.now());
        pollInternal();
    }

    @Override
    protected List<AccountOutboxEvent> fetchBatchAndMarkInProgress() {
        return accountOutboxService
                .fetchBatchAndMarkInProgress();
    }

    @Override
    protected String resolveTopic(AccountOutboxEvent event) {
        //return event.getAggregateType().toLowerCase() + "-events";
        return "account-events";
    }

    @Override
    protected void markSuccess(AccountOutboxEvent event) {
        accountOutboxService.markSuccess(event);
    }

    @Override
    protected void markRetry(AccountOutboxEvent event, int retryCount) {
        accountOutboxService.markRetry(event,
                retryCount);
    }

    @Override
    protected void markFailed(AccountOutboxEvent event) {
        accountOutboxService.markFailed(event);
    }

    @Override
    public int getRetryLimit() {
        return retryLimit;
    }
}
