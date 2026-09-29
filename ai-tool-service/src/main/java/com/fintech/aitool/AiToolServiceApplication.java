package com.fintech.aitool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.fintech.aitool"
        ,"com.fintech.common","com.fintech.security"})
@EnableFeignClients
public class AiToolServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiToolServiceApplication.class, args);
    }
}
