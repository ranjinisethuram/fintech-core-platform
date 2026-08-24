package com.fintech.orchestration.scheduler;

import com.fintech.common.command.*;
import com.fintech.common.exception.BaseException;
import com.fintech.orchestration.domain.Saga;
import com.fintech.orchestration.domain.SagaContext;
import com.fintech.orchestration.engine.OrchestrationEngine;
import com.fintech.orchestration.exception.OrchestrationErrorCode;
import com.fintech.orchestration.service.SagaService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SagaRecoveryService {

    private final SagaService sagaService;
    private final OrchestrationEngine orchestrationEngine;


    public SagaRecoveryService(SagaService sagaService, OrchestrationEngine orchestrationEngine) {
        this.sagaService = sagaService;
        this.orchestrationEngine = orchestrationEngine;
    }

    public void retryFailedSagaStep(Saga saga, UUID sagaRecoveryId){
        SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(saga.getSagaId());
        if(sagaContextEntity == null){
            throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
        }
        this.orchestrationEngine.recover(saga, sagaContextEntity, sagaRecoveryId);

//        switch (nextSagaState){
//            case ACCOUNT_CREATION -> {
//                SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(saga.getSagaId());
//                if(sagaContextEntity == null){
//                    throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
//                }
//                CustomerOnboardingContext customerOnboardingContext = this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
//                        CustomerOnboardingContext.class);
//                UUID accountId = UUID.randomUUID();
//                CreateAccountCommand createAccountCommand = new CreateAccountCommand(accountId,
//                        customerOnboardingContext.getCustomerId(),customerOnboardingContext.isDefaultAccount());
//                this.sagaService.buildMessageEnvelopeAndPushToOutbox(createAccountCommand,
//                        saga.getCorrelationId(),
//                        sagaRecoveryId.toString(),
//                        saga.getSagaId().toString(),
//                        false);
//            }
//            case WALLET_CREATION -> {
//                SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(saga.getSagaId());
//                if(sagaContextEntity == null){
//                    throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
//                }
//                CustomerOnboardingContext customerOnboardingContext = this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
//                        CustomerOnboardingContext.class);
//                UUID walletId = UUID.randomUUID();
//                CreateWalletCommand createWalletCommand = new CreateWalletCommand(
//                        walletId,
//                        customerOnboardingContext.getAccountId(),
//                        customerOnboardingContext.getCustomerId()
//                );
//                this.sagaService.buildMessageEnvelopeAndPushToOutbox(createWalletCommand,
//                        saga.getCorrelationId(),
//                        sagaRecoveryId.toString(),
//                        saga.getSagaId().toString(),
//                        false);
//            }
//            case LEDGER_ACCOUNT_CREATION -> {
//                SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(saga.getSagaId());
//                if(sagaContextEntity == null){
//                    throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
//                }
//                CustomerOnboardingContext customerOnboardingContext = this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
//                        CustomerOnboardingContext.class);
//                UUID ledgerAccountId = UUID.randomUUID();
//                CreateWalletLedgerAccountCommand createWalletLedgerAccountCommand = new CreateWalletLedgerAccountCommand(
//                        ledgerAccountId,
//                        customerOnboardingContext.getWalletId(),
//                        customerOnboardingContext.getAccountId(),
//                        customerOnboardingContext.getCustomerId(),
//                        customerOnboardingContext.getCurrency()
//                );
//                this.sagaService.buildMessageEnvelopeAndPushToOutbox(createWalletLedgerAccountCommand,
//                        saga.getCorrelationId(),
//                        sagaRecoveryId.toString(),
//                        saga.getSagaId().toString(),
//                        false);
//            }
//            case ACCOUNT_ACTIVATION -> {
//                SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(saga.getSagaId());
//                if(sagaContextEntity == null){
//                    throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
//                }
//                CustomerOnboardingContext customerOnboardingContext = this.sagaContextMapper.fromJson(sagaContextEntity.getContextJson(),
//                        CustomerOnboardingContext.class);
//                ActivateAccountCommand activateAccountCommand = new ActivateAccountCommand(
//                        customerOnboardingContext.getAccountId(),
//                        customerOnboardingContext.getCustomerId()
//                );
//                this.sagaService.buildMessageEnvelopeAndPushToOutbox(activateAccountCommand,
//                        saga.getCorrelationId(),
//                        sagaRecoveryId.toString(),
//                        saga.getSagaId().toString(),
//                        false);
//            }
//            case TRANSACTION_HANDLE -> {
//                SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(saga.getSagaId());
//                if(sagaContextEntity == null){
//                    throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
//                }
//                SagaContextType sagaContextType = sagaContextEntity.getSagaContextType();
//                switch (sagaContextType){
//                    case DEPOSIT -> {
//                        DepositContext depositContext = this.sagaContextMapper.fromJson(
//                                sagaContextEntity.getContextJson(),
//                                DepositContext.class);
//                        LedgerEntryRequest ledgerEntryRequest = new LedgerEntryRequest(LedgerEntryType.CREDIT,
//                                depositContext.getAccountId());
//                        CreateLedgerEntryCommand createLedgerEntryCommand = new CreateLedgerEntryCommand(depositContext.getTransactionId(),
//                                List.of(ledgerEntryRequest),
//                                depositContext.getAmount(),
//                                depositContext.getCurrency(),
//                                TransactionType.DEPOSIT);
//                        this.sagaService.buildMessageEnvelopeAndPushToOutbox(createLedgerEntryCommand,
//                                saga.getCorrelationId(),
//                                sagaRecoveryId.toString(),
//                                saga.getSagaId().toString(),
//                                false);
//                    }
//                }
//            }
//            case FUND_RESERVATION -> {
//
//            }
//            case LEDGER_ENTRIES_CREATION -> {
//
//            }
//            case WALLET_UPDATE -> {
//                SagaContext sagaContextEntity = this.sagaService.fetchSagaContext(saga.getSagaId());
//                if(sagaContextEntity == null){
//                    throw new BaseException(OrchestrationErrorCode.ORCH_SAGA_CONTEXT_NOT_FOUND);
//                }
//                SagaContextType sagaContextType = sagaContextEntity.getSagaContextType();
//                switch (sagaContextType){
//                    case DEPOSIT -> {
//                        DepositContext depositContext = this.sagaContextMapper.fromJson(
//                                sagaContextEntity.getContextJson(),
//                                DepositContext.class);
//                        CreditWalletCommand creditWalletCommand = new CreditWalletCommand(depositContext.getAccountId(),
//                                depositContext.getTransactionId(), depositContext.getAmount(),
//                                depositContext.getCurrency(),TransactionType.DEPOSIT);
//                        this.sagaService.buildMessageEnvelopeAndPushToOutbox(creditWalletCommand,
//                                saga.getCorrelationId(),
//                                sagaRecoveryId.toString(),
//                                saga.getSagaId().toString(),
//                                false);
//                    }
//                }
//            }
//        }

    }
}
