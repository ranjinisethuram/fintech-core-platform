package com.fintech.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.fintech.ledger","com.fintech.outbox"
        ,"com.fintech.common","com.fintech.security","com.fintech.ledgercontract","com.fintech.commoncontract"})
@EnableScheduling
public class LedgerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LedgerServiceApplication.class, args);
    }
}
