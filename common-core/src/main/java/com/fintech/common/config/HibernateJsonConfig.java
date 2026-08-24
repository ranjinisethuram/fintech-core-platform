package com.fintech.common.config;

import com.fintech.common.mapper.Jackson3FormatMapper;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class HibernateJsonConfig {

    @Bean
    public HibernatePropertiesCustomizer jsonFormatMapperCustomizer(JsonMapper jsonMapper) {
        // This injects existing JsonMapper bean into custom FormatMapper
        return properties -> properties.put(
                AvailableSettings.JSON_FORMAT_MAPPER,
                new Jackson3FormatMapper(jsonMapper)
        );
    }
}
