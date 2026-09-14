package com.fintech.transaction.feign;

import com.fintech.security.service.KeycloakTokenService;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    private final KeycloakTokenService keycloakTokenService;

    public FeignClientConfig(KeycloakTokenService keycloakTokenService) {
        this.keycloakTokenService = keycloakTokenService;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            // Trace propagation
            String traceId = MDC.get("traceId");
            if (traceId != null) {
                requestTemplate.header("X-Request-ID", traceId);
            }

            String token = keycloakTokenService.getToken();

            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}
