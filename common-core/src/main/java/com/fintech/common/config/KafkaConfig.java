package com.fintech.common.config;

import com.fintech.common.messaging.AuthContextRecordInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class KafkaConfig {

    private final AuthContextRecordInterceptor authContextRecordInterceptor;
    private final JsonMapper jsonMapper;

    private static final List<String> eventListForCustomer = List.of("AccountCreatedEvent","WalletCreatedEvent",
            "LedgerAccountCreatedEvent","AccountActivatedEvent", "AccountCreationSagaFailedEvent",
            "WalletCreationFailedEvent","LedgerAccountCreationFailedEvent",
            "AccountActivationFailedEvent","OrchestrationErrorEvent");

    private static final List<String> eventListForTransaction = List.of(
            "SagaSucceededEvent","SagaFailedEvent");

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
    public RecordFilterStrategy<String,Object> kafkaEventFilterForTransaction(){
        return record ->{
            String eventType = new String(record.headers().lastHeader("event-type").value());
            return eventListForTransaction.stream().noneMatch(eventType::equals);
        };
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
//        if(kafkaTransactionManager != null){
//            factory.getContainerProperties().setKafkaAwareTransactionManager(kafkaTransactionManager);
//        }
        factory.setRecordInterceptor(authContextRecordInterceptor);
//        factory.setRecordMessageConverter(new
//                org.springframework.kafka.support.converter.JacksonJsonMessageConverter(jsonMapper));

        factory.setRecordMessageConverter(new
                org.springframework.kafka.support.converter
                        .JacksonJsonMessageConverter(jsonMapper)
//                {
//                    @Override
//                    protected Object extractAndConvertValue(ConsumerRecord<?, ?> record
//                            , @Nullable Type type) {
//                        Object value = record.value();
//                        if (value instanceof String rawJsonString) {
//                            try {
//                                JsonNode rootNode = jsonMapper.readTree(rawJsonString);
//
//                                if (rootNode.isTextual()) {
//                                    // Safely extract the inner JSON string, stripping outer quotes and slashes
//                                    String cleanJson = rootNode.asText();
//
//                                    // Create a temporary cloned record wrapper carrying the normalized JSON text
//                                    ConsumerRecord<Object, Object> normalizedRecord = new ConsumerRecord<>(
//                                            record.topic(), record.partition(), record.offset(), record.key(), cleanJson
//                                    );
//                                    return super.extractAndConvertValue(normalizedRecord, type);
//                                }
//                            } catch (Exception e) {
//                                // Log the error and proceed to fallback to default behavior
//                                System.err.println("Error during JSON normalization: " + e.getMessage());
//                            }
//                        }
//                        return super.extractAndConvertValue(record, type);
//                    }
//                }
                );
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

    @Bean("dynamicEventContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> dynamicEventContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setRecordInterceptor(authContextRecordInterceptor);
        factory.setConsumerFactory(consumerFactory);
        //factory.setRecordMessageConverter(new JacksonJsonMessageConverter());

        return factory;
    }

    @Bean("debugKafkaErrorHandler")
    public KafkaListenerErrorHandler debugKafkaErrorHandler() {
        return (message, exception) -> {
            Throwable cause = exception.getCause();

            if (cause instanceof MismatchedInputException mismatchedEx) {
                String violatingFieldPath = mismatchedEx.getPath().stream()
                        .map(ref -> {
                            if (ref.getPropertyName() != null) {
                                return ref.getPropertyName();
                            }
                            if (ref.getIndex() >= 0) {
                                return "[" + ref.getIndex() + "]";
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("."));

                System.err.println("=================================================");
                System.err.println("JACKSON 3 DESERIALIZATION FIELD ANALYSIS");
                System.err.println("VIOLATING FIELD PATH: " + violatingFieldPath);
                System.err.println("TARGET EXPECTED TYPE: " + mismatchedEx.getTargetType().getName());
                System.err.println("=================================================");
            } else {
                System.err.println("OTHER ROUTING ERROR: " + exception.getMessage());
            }

            throw exception;
        };
    }
}
