package com.fintech.security;

import com.fintech.security.properties.KeycloakProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class CommonSecurityAutoConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, JsonMapper jsonMapper) {
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);
        return builder
                .additionalMessageConverters(converter)
                .build();
    }

}
