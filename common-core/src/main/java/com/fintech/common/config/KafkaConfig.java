package com.fintech.common.config;

import com.fintech.common.messaging.AuthContextRecordInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Configuration
public class KafkaConfig {

    private final AuthContextRecordInterceptor authContextRecordInterceptor;
    private final JsonMapper jsonMapper;

    private static final List<String> eventListForCustomer = List.of("AccountCreatedEvent","WalletCreatedEvent",
            "LedgerAccountCreatedEvent","AccountActivatedEvent", "AccountCreationSagaFailedEvent",
            "WalletCreationFailedEvent","LedgerAccountCreationFailedEvent","AccountActivationFailedEvent");

    public KafkaConfig(AuthContextRecordInterceptor authContextRecordInterceptor, JsonMapper jsonMapper) {
        this.authContextRecordInterceptor = authContextRecordInterceptor;
        this.jsonMapper = jsonMapper;
    }

    @Bean
    public RecordFilterStrategy<String,Object> kafkaEventFilterForCustomer(){
        return record ->{
            String eventType = new String(record.headers().lastHeader("event-type").value());
            return eventListForCustomer.stream().noneMatch(eventType::equals);
        };
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            @Autowired(required = false) KafkaTransactionManager<String, Object> kafkaTransactionManager) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        if(kafkaTransactionManager != null){
            factory.getContainerProperties().setKafkaAwareTransactionManager(kafkaTransactionManager);
        }
        factory.setRecordInterceptor(authContextRecordInterceptor);
        factory.setRecordMessageConverter(new
                org.springframework.kafka.support.converter.JacksonJsonMessageConverter(jsonMapper));

//        ExponentialBackOff backOff = new ExponentialBackOffWithMaxRetries(3);
//        backOff.setInitialInterval(500L);
//        backOff.setMultiplier(2.0);
//        backOff.setJitter(200L);
//        backOff.setMaxElapsedTime(5000L);
//        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(backOff);
//        defaultErrorHandler.addNotRetryableExceptions(DataIntegrityViolationException.class,
//                IllegalArgumentException.class);
//
//        factory.setCommonErrorHandler(defaultErrorHandler);
        return factory;
    }
}
