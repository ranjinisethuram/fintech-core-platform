package com.fintech.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fintech.common.messaging.AggregateMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration
public class JacksonConfig {

    @Primary
    @Bean
    public JsonMapper jsonMapper(){
        PolymorphicTypeValidator polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(AggregateMessage.class)
                .allowIfSubType("com.fintech.common.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.math.")
                .build();
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                // Crucial for Records and Enums inside Generics
                .changeDefaultVisibility(vc ->
                        vc.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                        .changeDefaultVisibility(vc ->
                                vc.withCreatorVisibility(JsonAutoDetect.Visibility.ANY))
                .findAndAddModules()
                .polymorphicTypeValidator(polymorphicTypeValidator)
                .build();

        // Optional: Store Instant as ISO-8601 string instead of timestamps
        //        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
