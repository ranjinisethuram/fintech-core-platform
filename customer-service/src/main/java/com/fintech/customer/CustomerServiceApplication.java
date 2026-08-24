package com.fintech.customer;

import com.fintech.outbox.BaseOutboxEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.fintech.customer","com.fintech.outbox"
        ,"com.fintech.common","com.fintech.security","com.fintech.accountcontract","com.fintech.commoncontract"})
@EnableScheduling
@EnableFeignClients
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}

