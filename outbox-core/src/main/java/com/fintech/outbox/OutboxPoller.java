package com.fintech.outbox;

import com.fintech.common.messaging.KafkaEventProducer;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.scheduling.annotation.Scheduled;


import java.util.List;
import java.util.UUID;

public  abstract class OutboxPoller<T extends BaseOutboxEvent> {

    private final KafkaEventProducer kafkaEventProducer;

    protected OutboxPoller(KafkaEventProducer kafkaEventProducer) {
        this.kafkaEventProducer = kafkaEventProducer;
    }

    protected abstract List<T> fetchBatchAndMarkInProgress();

    protected abstract String resolveTopic(T event);

    protected abstract void markSuccess(T event);

    protected abstract void markRetry(T event, int retryCount);

    protected abstract void markFailed(T event);

    protected abstract int getRetryLimit();

    protected void pollInternal() {

        List<T> events = fetchBatchAndMarkInProgress();

        for (T event : events) {
//            try {
//                publish(event);
//                markProcessed(event);
//            } catch (Exception ex) {
//                handleFailure(event, ex);
//            }
            kafkaEventProducer.sendAsync(
                    resolveTopic(event),
                    event.getAggregateId(),
                    event.getEventType(),
                    event.getPayload()
            ).whenComplete((result, ex) -> {

                if (ex == null) {
                    System.out.println("Inside success callback");
                    markSuccess(event);
                    //log.info("Event processed successfully for {}", event,getId())
                } else {
                    handleFailure(event, ex);
                }
            });
        }
    }

    private void handleFailure(T event, Throwable ex) {
        if (isPermanent(ex)) {
            System.out.println("Inside failure callback");
            markFailed(event);
//            log.error("Permanent failure for {}", event.getId(), ex);
            return;
        }

        if (!isRetryable(event)) {
            System.out.println("Inside non-retryable callback");
            markFailed(event);
//            log.error("Max retries exceeded for {}", event.getId());
            return;
        }
        System.out.println("Inside retryable callback");
        markRetry(event, event.getRetryCount());
    }

    private boolean isPermanent(Throwable ex) {
        return ex instanceof SerializationException
                || ex instanceof IllegalArgumentException
                || ex instanceof AuthenticationException
                || ex instanceof AuthorizationException
                ;
    }

    private boolean isRetryable(T event) {
        return event.getRetryCount() < getRetryLimit();
    }

//    private Duration exponentialBackoff(int retry) {
//        return Duration.ofSeconds((long) Math.pow(2, retry));
//    }

//    private String resolveTopic(T event) {
//        return event.getAggregateType().toLowerCase() + "-events";
//    }

//    @Transactional
//    public void markProcessed(UUID eventId) {
//        baseOutboxRepository.findById(eventId)
//                .ifPresent(BaseOutboxEvent::markProcessed);
//    }
//
//    @Transactional
//    public void markFailed(UUID eventId) {
//        baseOutboxRepository.findById(eventId)
//                .ifPresent(BaseOutboxEvent::markFailed);
//    }
}
