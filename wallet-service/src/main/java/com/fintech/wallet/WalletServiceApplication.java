package com.fintech.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.fintech.wallet","com.fintech.outbox"
        ,"com.fintech.common","com.fintech.security","com.fintech.walletcontract","com.fintech.commoncontract"})
@EnableScheduling
public class WalletServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }
}