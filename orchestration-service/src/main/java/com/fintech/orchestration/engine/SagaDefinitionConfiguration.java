package com.fintech.orchestration.engine;

import com.fintech.orchestration.domain.SagaType;
import com.fintech.orchestration.domain.StepId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SagaDefinitionConfiguration {

    @Bean
    public SagaDefinitionRegistry sagaDefinitionRegistry() {

        SagaDefinitionRegistry registry =
                new SagaDefinitionRegistry();

        registry.register(
                new SagaDefinition(
                        SagaType.CUSTOMER_ONBOARDING,
                        List.of(
                                StepId.CREATE_ACCOUNT,
                                StepId.CREATE_WALLET,
                                StepId.CREATE_LEDGER_ACCOUNT,
                                StepId.ACTIVATE_ACCOUNT
                        )
                ));

        registry.register(
                new SagaDefinition(
                        SagaType.DEPOSIT,
                        List.of(
                                StepId.CREATE_LEDGER_ENTRIES,
                                StepId.CREDIT_WALLET
                        )
                ));

        registry.register(
                new SagaDefinition(
                        SagaType.WITHDRAWAL,
                        List.of(
                                StepId.FRAUD_CHECK,
                                StepId.RESERVE_FUNDS,
                                StepId.CREATE_LEDGER_ENTRIES,
                                StepId.COMMIT_FUNDS
                        )
                ));

        registry.register(
                new SagaDefinition(
                        SagaType.TRANSFER,
                        List.of(
                                StepId.FRAUD_CHECK,
                                StepId.RESERVE_FUNDS,
                                StepId.CREATE_LEDGER_ENTRIES,
                                StepId.FUND_TRANSFER
                        )
                ));

        return registry;
    }
}
