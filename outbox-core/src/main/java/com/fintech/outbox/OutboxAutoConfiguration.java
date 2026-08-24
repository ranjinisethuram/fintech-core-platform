package com.fintech.outbox;

import com.fintech.common.messaging.KafkaEventProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@ConditionalOnProperty(name = "outbox.enabled", havingValue = "true", matchIfMissing = true)
//public class OutboxAutoConfiguration {
//
//    @Bean
//    public OutboxPoller<?> outboxPoller(
//            BaseOutboxRepository<?> repository,
//            KafkaEventProducer kafkaEventProducer) {
//
//        return new OutboxPoller<>(repository, kafkaEventProducer);
//    }
//}
