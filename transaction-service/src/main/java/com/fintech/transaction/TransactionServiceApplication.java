package com.fintech.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.fintech.transaction","com.fintech.outbox"
        ,"com.fintech.common","com.fintech.security","com.fintech.transactioncontract","com.fintech.accountcontract","com.fintech.commoncontract",
        "com.fintech.ledgercontract","com.fintech.walletcontract"})
@EnableScheduling
@EnableFeignClients
public class TransactionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
