package com.fintech.transaction.service;

import com.fintech.accountcontract.domain.AccountValidationFieldConstant;
import com.fintech.accountcontract.dto.AccountValidationRequest;
import com.fintech.accountcontract.dto.AccountValidationResponse;
import com.fintech.common.domain.TransactionType;
import com.fintech.common.event.TransactionInitiatedEvent;
import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.common.exception.FieldErrorDetail;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.commoncontract.ApiErrorLiterals;
import com.fintech.outbox.OutboxMapper;
import com.fintech.transaction.domain.Transaction;
import com.fintech.transaction.domain.TransactionStatus;
import com.fintech.transaction.dto.TransactionHistory;
import com.fintech.transaction.dto.TransactionRequest;
import com.fintech.transaction.dto.TransactionResponse;
import com.fintech.transaction.exception.TransactionErrorCode;
import com.fintech.transaction.feign.account.AccountValidationAdapter;
import com.fintech.transaction.feign.wallet.WalletValidationAdapter;
import com.fintech.transaction.outbox.TransactionOutboxEvent;
import com.fintech.transaction.outbox.TransactionOutboxRepository;
import com.fintech.transaction.repository.TransactionRepository;
import com.fintech.walletcontract.dto.WalletAccountValidationRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionOutboxRepository transactionOutboxRepository;
    private final OutboxMapper mapper;
    private final AccountValidationAdapter accountAdapter;
    private final MessageEnvelopeFactory messageEnvelopeFactory;

    private static final UUID NIL_UUID = new UUID(0L, 0L);

    public TransactionService(TransactionRepository transactionRepository, TransactionOutboxRepository transactionOutboxRepository, OutboxMapper mapper
            , AccountValidationAdapter accountAdapter, MessageEnvelopeFactory messageEnvelopeFactory) {
        this.transactionRepository = transactionRepository;
        this.transactionOutboxRepository = transactionOutboxRepository;
        this.mapper = mapper;
        this.accountAdapter = accountAdapter;
        this.messageEnvelopeFactory = messageEnvelopeFactory;
    }

    @Transactional
    public TransactionResponse depositAmount(TransactionRequest transactionRequest,
                     UUID customerId, String externalReferenceId) throws BaseException{
        validateTransactionRequest(transactionRequest, TransactionType.DEPOSIT);
        boolean isValid = isAccountValid(transactionRequest,customerId, TransactionType.DEPOSIT);
        if(!isValid){
            throw new BaseException(TransactionErrorCode.TRANSACTION_INITIATION_FAILED);
        }
        Transaction transaction = new Transaction(customerId,transactionRequest.getSourceAccountId()
                , transactionRequest.getDestinationAccountId(), transactionRequest.getAmount()
                , transactionRequest.getCurrency(), externalReferenceId, TransactionType.DEPOSIT);
        try{
            UUID transactionId = this.transactionRepository.saveAndFlush(transaction).getTransactionId();
            String requestId = "transaction"+"_"+UUID.randomUUID();
            TransactionInitiatedEvent transactionInitiatedEvent = new TransactionInitiatedEvent(
                    transactionId.toString(),
                    transaction.getSourceAccountId().toString(),
                    transaction.getDestinationAccountId().toString(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    TransactionType.DEPOSIT);
            MessageEnvelope<TransactionInitiatedEvent> messageEnvelope =
                    this.messageEnvelopeFactory.build(requestId,"",""
                    ,"TransactionService",transactionInitiatedEvent);
            TransactionOutboxEvent transactionOutboxEvent = mapper
                    .mapToOutboxEvent(messageEnvelope, TransactionOutboxEvent::new);
            this.transactionOutboxRepository.saveAndFlush(transactionOutboxEvent);
            return new TransactionResponse(transactionId.toString(),
                    TransactionStatus.INITIATED.getStatus(),
                    TransactionStatus.fetchDetailFromStatus(TransactionStatus.INITIATED.getStatus()));
        }catch(DataIntegrityViolationException dataIntegrityViolationException){
            throw new BaseException(TransactionErrorCode.DUPLICATE_TRANSACTION);
        } catch (Exception exception){
            throw new BaseException(CommonErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public TransactionResponse withdrawAmount(TransactionRequest transactionRequest
            , UUID customerId, String externalReferenceId) {
        validateTransactionRequest(transactionRequest, TransactionType.WITHDRAWAL);
        boolean isValid = isAccountValid(transactionRequest,customerId, TransactionType.WITHDRAWAL);
        if(!isValid){
            throw new BaseException(TransactionErrorCode.TRANSACTION_INITIATION_FAILED);
        }
        Transaction transaction = new Transaction(customerId,transactionRequest.getSourceAccountId()
                , transactionRequest.getDestinationAccountId(), transactionRequest.getAmount()
                , transactionRequest.getCurrency(), externalReferenceId, TransactionType.WITHDRAWAL);
        try{
            UUID transactionId = this.transactionRepository.saveAndFlush(transaction).getTransactionId();
            String requestId = "transaction"+"_"+UUID.randomUUID();
            TransactionInitiatedEvent transactionInitiatedEvent = new TransactionInitiatedEvent(
                    transactionId.toString(),
                    transaction.getSourceAccountId().toString(),
                    transaction.getDestinationAccountId().toString(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    TransactionType.WITHDRAWAL);
            MessageEnvelope<TransactionInitiatedEvent> messageEnvelope =
                    this.messageEnvelopeFactory.build(requestId,"",""
                            ,"TransactionService",transactionInitiatedEvent);
            TransactionOutboxEvent transactionOutboxEvent = mapper
                    .mapToOutboxEvent(messageEnvelope, TransactionOutboxEvent::new);
            this.transactionOutboxRepository.saveAndFlush(transactionOutboxEvent);
            return new TransactionResponse(transactionId.toString(),
                    TransactionStatus.INITIATED.getStatus(),
                    TransactionStatus.fetchDetailFromStatus(TransactionStatus.INITIATED.getStatus()));
        }catch(DataIntegrityViolationException dataIntegrityViolationException){
            throw new BaseException(TransactionErrorCode.DUPLICATE_TRANSACTION);
        }catch (Exception exception){
            throw new BaseException(CommonErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public TransactionResponse transferAmount(TransactionRequest transactionRequest
            , UUID customerId, String externalReferenceId){
        validateTransactionRequest(transactionRequest, TransactionType.TRANSFER);
        boolean isValid = isAccountValid(transactionRequest,customerId, TransactionType.TRANSFER);
        if(!isValid){
            throw new BaseException(TransactionErrorCode.TRANSACTION_INITIATION_FAILED);
        }
        Transaction transaction = new Transaction(customerId,transactionRequest.getSourceAccountId()
                , transactionRequest.getDestinationAccountId(), transactionRequest.getAmount()
                , transactionRequest.getCurrency(), externalReferenceId, TransactionType.TRANSFER);
        try{
            UUID transactionId = this.transactionRepository.saveAndFlush(transaction).getTransactionId();
            String requestId = "transaction"+"_"+UUID.randomUUID();
            TransactionInitiatedEvent transactionInitiatedEvent = new TransactionInitiatedEvent(
                    transactionId.toString(),
                    transaction.getSourceAccountId().toString(),
                    transaction.getDestinationAccountId().toString(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    TransactionType.TRANSFER);
            MessageEnvelope<TransactionInitiatedEvent> messageEnvelope =
                    this.messageEnvelopeFactory.build(requestId,"",""
                            ,"TransactionService",transactionInitiatedEvent);
            TransactionOutboxEvent transactionOutboxEvent = mapper
                    .mapToOutboxEvent(messageEnvelope, TransactionOutboxEvent::new);
            this.transactionOutboxRepository.saveAndFlush(transactionOutboxEvent);
            return new TransactionResponse(transactionId.toString(),
                    TransactionStatus.INITIATED.getStatus(),
                    TransactionStatus.fetchDetailFromStatus(TransactionStatus.INITIATED.getStatus()));
        }catch(DataIntegrityViolationException dataIntegrityViolationException){
            throw new BaseException(TransactionErrorCode.DUPLICATE_TRANSACTION);
        }catch (Exception exception){
            throw new BaseException(CommonErrorCode.INTERNAL_ERROR);
        }
    }

    public TransactionHistory fetchTransactionHistory(UUID sourceAccountId, Instant createdAfter){
        List<Transaction> transactionHistory = this.transactionRepository.findBySourceAccountIdAndCreatedAtAfter(sourceAccountId, createdAfter);
        transactionHistory.stream().
    }

    private boolean isAccountValid(TransactionRequest transactionRequest, UUID customerId,
                                   TransactionType transactionType) {
        AccountValidationRequest accountValidationRequest = new AccountValidationRequest();
        accountValidationRequest.setCustomerId(customerId);
        accountValidationRequest.setTransactionType(transactionType);
        accountValidationRequest.setSourceAccountId(transactionRequest.getSourceAccountId());
        accountValidationRequest.setDestinationAccountId(transactionRequest.getDestinationAccountId());
        AccountValidationResponse accountValidationResponse = new AccountValidationResponse();
        try {
            accountValidationResponse = accountAdapter.validateAccount(accountValidationRequest);
        }catch(BaseException exception){
            FieldErrorDetail fieldErrorDetail = exception.getFieldErrorDetailList().getFirst();
            TransactionErrorCode transactionErrorCode= resolveAccountValidationFieldErrorDetail(fieldErrorDetail);
            //throw new BaseException(transactionErrorCode);
            //dont throw, just log the exception here.
            // The upstream method will return a Transaction failed exception.
        }
        return accountValidationResponse.getStatus();
    }

    private TransactionErrorCode resolveAccountValidationFieldErrorDetail(FieldErrorDetail fieldErrorDetail) {
        TransactionErrorCode transactionErrorCode = TransactionErrorCode.VALIDATION_FAILED;
        boolean isSourceField = fieldErrorDetail.getField().equals(AccountValidationFieldConstant.sourceAccountId.name());
        switch (fieldErrorDetail.getReason()){
            case String errorLiteral when isSourceField
                    && (errorLiteral.equals(ApiErrorLiterals.NOT_FOUND)) -> {
                transactionErrorCode = TransactionErrorCode.SOURCE_ACCOUNT_NOT_FOUND;
            }
            case String errorLiteral when !isSourceField
                    && (errorLiteral.equals(ApiErrorLiterals.NOT_FOUND)) -> {
                transactionErrorCode = TransactionErrorCode.DESTINATION_ACCOUNT_NOT_FOUND;
            }
            case String errorLiteral when isSourceField
                    && (errorLiteral.equals(ApiErrorLiterals.INACTIVE)) -> {
                transactionErrorCode = TransactionErrorCode.SOURCE_ACCOUNT_NOT_ACTIVE;
            }
            case String errorLiteral when !isSourceField
                    && (errorLiteral.equals(ApiErrorLiterals.INACTIVE)) -> {
                transactionErrorCode = TransactionErrorCode.DESTINATION_ACCOUNT_NOT_ACTIVE;
            }
            case String errorLiteral when isSourceField
                    && (errorLiteral.equals(ApiErrorLiterals.UNAUTHORIZED)) -> {
                transactionErrorCode = TransactionErrorCode.INVALID_SOURCE_ACCOUNT;
            }
            default -> {
            }
        }
        return transactionErrorCode;
    }

    private void validateTransactionRequest(TransactionRequest transactionRequest, TransactionType transactionType) {
        switch (transactionType){
            case DEPOSIT -> {
                if(transactionRequest.getDestinationAccountId() == null ||
                        transactionRequest.getDestinationAccountId().equals(NIL_UUID)){
                    throw new BaseException(TransactionErrorCode.DESTINATION_ACCOUNT_ID_MISSING);
                }
            }
            case WITHDRAWAL -> {
                if(transactionRequest.getSourceAccountId() == null ||
                        transactionRequest.getSourceAccountId().equals(NIL_UUID)){
                    throw new BaseException(TransactionErrorCode.SOURCE_ACCOUNT_ID_MISSING);
                }
            }
            case TRANSFER -> {
                if(transactionRequest.getSourceAccountId() == null ||
                        transactionRequest.getSourceAccountId().equals(NIL_UUID)){
                    throw new BaseException(TransactionErrorCode.SOURCE_ACCOUNT_ID_MISSING);
                }else if(transactionRequest.getDestinationAccountId() == null ||
                        transactionRequest.getDestinationAccountId().equals(NIL_UUID)){
                    throw new BaseException(TransactionErrorCode.DESTINATION_ACCOUNT_ID_MISSING);
                }else if((transactionRequest.getSourceAccountId() == null ||
                        transactionRequest.getSourceAccountId().equals(NIL_UUID)) &&
                        (transactionRequest.getDestinationAccountId() == null ||
                        transactionRequest.getDestinationAccountId().equals(NIL_UUID))){
                    throw new BaseException(TransactionErrorCode.ACCOUNT_IDS_MISSING);
                }
            }
        }
    }
}
