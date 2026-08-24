package com.fintech.account.service;

import com.fintech.account.domain.Account;
import com.fintech.account.domain.AccountStatus;
import com.fintech.accountcontract.domain.AccountType;
import com.fintech.account.exception.AccountErrorCode;
import com.fintech.account.outbox.AccountOutboxEvent;
import com.fintech.account.outbox.AccountOutboxRepository;
import com.fintech.account.repository.AccountRepository;
import com.fintech.accountcontract.domain.AccountValidationFieldConstant;
import com.fintech.accountcontract.dto.AccountSummary;
import com.fintech.accountcontract.dto.AccountValidationRequest;
import com.fintech.accountcontract.dto.AccountValidationResponse;
import com.fintech.common.command.ActivateAccountCommand;
import com.fintech.common.command.CreateAccountCommand;
import com.fintech.common.domain.TransactionType;
import com.fintech.common.event.AccountActivatedEvent;
import com.fintech.common.event.AccountActivationFailedEvent;
import com.fintech.common.event.AccountCreatedEvent;
import com.fintech.common.event.AccountCreationSagaFailedEvent;
import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.FieldErrorDetail;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.commoncontract.ApiErrorLiterals;
import com.fintech.outbox.OutboxMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountOutboxRepository outboxRepository;
    private final OutboxMapper outboxMapper;
    private final MessageEnvelopeFactory messageEnvelopeFactory;
    private final UUID zeroUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public AccountService (AccountRepository repository,
                           AccountOutboxRepository outboxRepository, OutboxMapper outboxMapper, MessageEnvelopeFactory messageEnvelopeFactory){
        this.accountRepository = repository;
        this.outboxRepository = outboxRepository;
        this.outboxMapper = outboxMapper;
        this.messageEnvelopeFactory = messageEnvelopeFactory;
    }

    @Transactional
    public void createAccount (CreateAccountCommand createAccountCommand,
                                  String correlationId, String causationId, String sagaId){
        try {
            Account account = this.accountRepository.saveAndFlush(new Account(createAccountCommand.accountId()
                    ,createAccountCommand.customerId(), AccountType.SAVINGS, createAccountCommand.isDefault()));
            AccountCreatedEvent accountCreatedEvent = new AccountCreatedEvent(
                    account.getId().toString(), account.getCustomerId().toString(), account.isDefault(), Instant.now());
            buildMessageEnvelopeAndPushToOutbox(accountCreatedEvent,correlationId,causationId,sagaId);

        }catch(DataIntegrityViolationException dataIntegrityViolationException){
            AccountCreationSagaFailedEvent accountCreationSagaFailedEvent = new AccountCreationSagaFailedEvent(
                    createAccountCommand.accountId().toString(),
                    createAccountCommand.customerId().toString(),
                    AccountErrorCode.ACCOUNT_ALREADY_EXISTS.getErrorCode(),
                    AccountErrorCode.ACCOUNT_ALREADY_EXISTS.getErrorMessage(),
                    AccountErrorCode.ACCOUNT_ALREADY_EXISTS.isRetryable(),
                    Instant.now());
            buildMessageEnvelopeAndPushToOutbox(accountCreationSagaFailedEvent,correlationId,causationId,sagaId);
        }
    }

    @Transactional
    public Account fetchAccountById(UUID accountId){
        Optional<Account> currentAccount = this.accountRepository.findById(accountId);
        return currentAccount.orElse(null);
    }

    @Transactional
    public void activateAccount(ActivateAccountCommand activateAccountCommand,
                                String correlationId, String causationId, String sagaId){
        Account account = fetchAccountById(activateAccountCommand.accountId());
        if(account != null){
            account.setAccountStatus(AccountStatus.ACTIVE);
            Account accountUpdated = this.accountRepository.saveAndFlush(account);
            AccountActivatedEvent accountActivatedEvent = new AccountActivatedEvent(
                    accountUpdated.getId().toString(),
                    activateAccountCommand.customerId().toString(),Instant.now());
            buildMessageEnvelopeAndPushToOutbox(accountActivatedEvent
                    ,correlationId,causationId,sagaId);
        }else{
            //Account not found
            AccountActivationFailedEvent accountActivationSagaFailedEvent = new AccountActivationFailedEvent(
                    activateAccountCommand.accountId().toString(),
                    activateAccountCommand.customerId().toString(),
                    AccountErrorCode.ACCOUNT_NOT_EXISTS.getErrorCode(),
                    AccountErrorCode.ACCOUNT_NOT_EXISTS.getErrorMessage(),
                    AccountErrorCode.ACCOUNT_NOT_EXISTS.isRetryable(),
                    Instant.now());
            buildMessageEnvelopeAndPushToOutbox(accountActivationSagaFailedEvent
                    ,correlationId,causationId,sagaId);
        }

    }

    public AccountValidationResponse validateAccount(AccountValidationRequest accountValidationRequest){
        AccountValidationResponse accountValidationResponse = new AccountValidationResponse();
        TransactionType transactionType = accountValidationRequest.getTransactionType();
        switch (transactionType) {
            case DEPOSIT -> {
                UUID destinationAccountId =
                        accountValidationRequest.getDestinationAccountId();
                Account destinationAccount = fetchAccounts(List.of(destinationAccountId)).stream()
                        .findFirst().orElse(null);
                boolean isValid = false;
                try {
                    isValid = validateAccount(destinationAccount);
                }catch(BaseException ex){
                    FieldErrorDetail fieldErrorDetail = new FieldErrorDetail(AccountValidationFieldConstant
                            .destinationAccountId.name(),
                            mapErrorCodesToLiterals((AccountErrorCode) ex.getErrorCode()));
                    throw new BaseException(ex.getErrorCode(),new ArrayList<>(List.of(fieldErrorDetail)));
                }
                accountValidationResponse.setStatus(isValid);
            }
            case WITHDRAWAL -> {
                UUID sourceAccountId = accountValidationRequest.getSourceAccountId();
                Account sourceAccount = fetchAccounts(List.of(sourceAccountId)).stream()
                        .findFirst().orElse(null);
                boolean isValid = false;
                try {
                    isValid = validateAccount(sourceAccount);
                }catch(BaseException ex){
                    FieldErrorDetail fieldErrorDetail = new FieldErrorDetail(AccountValidationFieldConstant
                            .sourceAccountId.name(),
                            mapErrorCodesToLiterals((AccountErrorCode) ex.getErrorCode()));
                    throw new BaseException(ex.getErrorCode(),
                            new ArrayList<>(List.of(fieldErrorDetail)));
                }
                if(isValid &&
                        !isAccountAuthorized(sourceAccount, accountValidationRequest.getCustomerId())){
                    accountValidationResponse.setStatus(false);
                    FieldErrorDetail fieldErrorDetail = new FieldErrorDetail(AccountValidationFieldConstant
                            .sourceAccountId.name(),
                            mapErrorCodesToLiterals(AccountErrorCode.ACCOUNT_ILLEGAL_ACCESS));
                    throw new BaseException(AccountErrorCode.ACCOUNT_ILLEGAL_ACCESS,
                            new ArrayList<>(List.of(fieldErrorDetail)));
                }
                accountValidationResponse.setStatus(isValid);
            }
            case TRANSFER -> {
                UUID sourceAccountId = accountValidationRequest.getSourceAccountId();
                UUID destinationAccountId =
                        accountValidationRequest.getDestinationAccountId();
                List<Account> requestedAccounts = fetchAccounts(List.of(sourceAccountId
                        ,destinationAccountId));
                Account sourceAccount = requestedAccounts.stream()
                        .filter(account -> account.getId().equals(sourceAccountId))
                        .findFirst().orElse(null);
                Account destinationAccount = requestedAccounts.stream()
                        .filter(account -> account.getId().equals(destinationAccountId))
                        .findFirst().orElse(null);
                boolean isValid = false;
                try {
                    isValid = validateAccount(sourceAccount);
                }catch(BaseException ex){
                    FieldErrorDetail fieldErrorDetail = new FieldErrorDetail(AccountValidationFieldConstant
                            .sourceAccountId.name(),
                            mapErrorCodesToLiterals((AccountErrorCode) ex.getErrorCode()));
                    throw new BaseException(ex.getErrorCode(),
                            new ArrayList<>(List.of(fieldErrorDetail)));
                }
                if(isValid){
                    if(!isAccountAuthorized(sourceAccount, accountValidationRequest.getCustomerId())){
                        accountValidationResponse.setStatus(false);
                        FieldErrorDetail fieldErrorDetail = new FieldErrorDetail(AccountValidationFieldConstant
                                .sourceAccountId.name(),
                                mapErrorCodesToLiterals(AccountErrorCode.ACCOUNT_ILLEGAL_ACCESS));
                        throw new BaseException(AccountErrorCode.ACCOUNT_ILLEGAL_ACCESS,
                                new ArrayList<>(List.of(fieldErrorDetail)));
                    } else{
                        try {
                            isValid = validateAccount(destinationAccount);
                        }catch(BaseException ex){
                            FieldErrorDetail fieldErrorDetail = new FieldErrorDetail(AccountValidationFieldConstant
                                    .destinationAccountId.name(),
                                    mapErrorCodesToLiterals((AccountErrorCode) ex.getErrorCode()));
                            throw new BaseException(ex.getErrorCode(),
                                    new ArrayList<>(List.of(fieldErrorDetail)));
                        }
                    }
                }
                accountValidationResponse.setStatus(isValid);
            }
        }
        return accountValidationResponse;
    }

    public List<AccountSummary> fetchCustomerAccountDetails(UUID customerId){
        List<Account> customerAccounts = this.accountRepository.findByCustomerId(customerId);
        List<AccountSummary> customerAccountsSummary = new ArrayList<>();
        for(Account customerAccount: customerAccounts){
            AccountSummary accountSummary = new AccountSummary();
            accountSummary.setAccountId(customerAccount.getId().toString());
            accountSummary.setAccountType(customerAccount.getAccountType().name());
            accountSummary.setDefault(customerAccount.isDefault());
            customerAccountsSummary.add(accountSummary);
        }
        return customerAccountsSummary;
    }

    private List<Account> fetchAccounts(List<UUID> accountIds){
        return this.accountRepository.findAllById(accountIds);
    }

    private boolean validateAccount(Account account){
        boolean isValid = false;
        if(account == null) {
            throw new BaseException(AccountErrorCode.ACCOUNT_NOT_EXISTS);
        } else{
            AccountStatus accountStatus = account.getAccountStatus();
            if(!accountStatus.name().equals("ACTIVE")){
                throw new BaseException(AccountErrorCode.ACCOUNT_NOT_ACTIVE);
            }else{
                isValid = true;
            }
        }
        return isValid;
    }

    private boolean isAccountAuthorized(Account account, UUID customerId){
        return customerId
                .compareTo(account.getCustomerId()) == 0;
    }

    private String mapErrorCodesToLiterals(AccountErrorCode errorCode){
        String errorLiteral = "";
        switch (errorCode){
            case ACCOUNT_NOT_EXISTS -> {
                errorLiteral = ApiErrorLiterals.NOT_FOUND;
            }
            case ACCOUNT_NOT_ACTIVE -> {
                errorLiteral = ApiErrorLiterals.INACTIVE;
            }
            case ACCOUNT_ILLEGAL_ACCESS -> {
                errorLiteral = ApiErrorLiterals.UNAUTHORIZED;
            }
            default -> {
                errorLiteral = ApiErrorLiterals.UNKNOWN_ERROR;
            }
        }
        return errorLiteral;
    }

    private <T extends AggregateMessage> void buildMessageEnvelopeAndPushToOutbox(T eventMessage
            , String correlationId, String causationId, String sagaId){
        MessageEnvelope<T> messageEnvelope =
                this.messageEnvelopeFactory
                        .build(correlationId,
                                causationId,
                                sagaId,
                                "AccountService",
                                eventMessage);
        AccountOutboxEvent outboxEvent =
                outboxMapper.mapToOutboxEvent(
                        messageEnvelope,
                        AccountOutboxEvent::new
                );
        this.outboxRepository.saveAndFlush(outboxEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends AggregateMessage> void writeFailedEventMessageToOutbox(T eventMessage
            , String correlationId, String causationId, String sagaId){
        this.buildMessageEnvelopeAndPushToOutbox(eventMessage,correlationId,causationId,sagaId);
    }
}
