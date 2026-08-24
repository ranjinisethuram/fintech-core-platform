package com.fintech.customer.listener;

import com.fintech.common.event.*;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.customer.domain.CustomerStatus;
import com.fintech.customer.repository.ProcessedMessagesRepository;
import com.fintech.customer.service.CustomerService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventListener {

    private final CustomerService customerService;
    private final ProcessedMessagesRepository processedMessagesRepository;
    private final JsonMapper jsonMapper;

    public EventListener(CustomerService customerService, ProcessedMessagesRepository processedMessagesRepository, JsonMapper jsonMapper) {
        this.customerService = customerService;
        this.processedMessagesRepository = processedMessagesRepository;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(topics = {"account-events","wallet-events","ledger-events"},
            groupId = "customer-service",
            filter = "kafkaEventFilterForCustomer")
    public void handle(MessageEnvelope<?> messageEnvelope){
        int rowsInserted = processedMessagesRepository
                .insert(messageEnvelope.getMessageId(), Instant.now());
        if(rowsInserted == 0) {
            return;
        }
        UUID customerId = null;
        String status = "";
        if(messageEnvelope.getPayload() instanceof AccountCreatedEvent){
            AccountCreatedEvent accountCreatedEvent = (AccountCreatedEvent) messageEnvelope.getPayload();
            customerId = UUID.fromString(accountCreatedEvent.customerId());
            status = CustomerStatus.ACCOUNT_CREATED.getStatus();
        }else if(messageEnvelope.getPayload() instanceof WalletCreatedEvent){
            WalletCreatedEvent walletCreatedEvent = (WalletCreatedEvent) messageEnvelope.getPayload();
            customerId = UUID.fromString(walletCreatedEvent.customerId());
            status = CustomerStatus.WALLET_CREATED.getStatus();
        }else if(messageEnvelope.getPayload() instanceof LedgerAccountCreatedEvent){
            LedgerAccountCreatedEvent ledgerAccountCreatedEvent = (LedgerAccountCreatedEvent) messageEnvelope.getPayload();
            customerId = UUID.fromString(ledgerAccountCreatedEvent.customerId());
            status = CustomerStatus.LEDGER_ACCOUNT_CREATED.getStatus();
        }else if(messageEnvelope.getPayload() instanceof AccountActivatedEvent){
            AccountActivatedEvent accountActivatedEvent = (AccountActivatedEvent) messageEnvelope.getPayload();
            customerId = UUID.fromString(accountActivatedEvent.customerId());
            status = CustomerStatus.ACCOUNT_ACTIVATED.getStatus();
        }else if(messageEnvelope.getPayload() instanceof AccountCreationSagaFailedEvent ||
        messageEnvelope.getPayload() instanceof WalletCreationFailedEvent ||
        messageEnvelope.getPayload() instanceof  LedgerAccountCreationFailedEvent ||
        messageEnvelope.getPayload() instanceof  AccountActivationFailedEvent) {
            //Customer onboarding failed
            String jsonStr = this.jsonMapper.writeValueAsString(messageEnvelope.getPayload());
            JsonNode rootNode = jsonMapper.readTree(jsonStr);
            customerId = rootNode.get("customerId").asString("") != "" ?
            UUID.fromString(rootNode.get("customerId").asString()) : null;
            status = CustomerStatus.CUSTOMER_ONBOARDING_FAILED.getStatus();
        }
        if(customerId == null || status.isEmpty())
            return;
        this.customerService.updateCustomerStatus(customerId,status);
    }
}
