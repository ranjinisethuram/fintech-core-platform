package com.fintech.customer.service;

import com.fintech.accountcontract.dto.AccountSummary;
import com.fintech.accountcontract.dto.CustomerAccountsResponse;
import com.fintech.common.event.CustomerCreatedEvent;
import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.customer.domain.Customer;
import com.fintech.customer.domain.CustomerStatus;
import com.fintech.customer.dto.CreateCustomerRequest;
import com.fintech.customer.dto.CustomerProfile;
import com.fintech.customer.dto.CustomerStatusResponse;
import com.fintech.customer.exception.CustomerErrorCode;
import com.fintech.customer.feign.account.AccountQueryAdapter;
import com.fintech.customer.outbox.CustomerOutboxEvent;
import com.fintech.customer.outbox.CustomerOutboxRepository;
import com.fintech.customer.repository.CustomerRepository;
import com.fintech.customer.repository.ProcessedMessagesRepository;
import com.fintech.outbox.OutboxMapper;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fintech.customer.repository.BeneficiaryRepository;
import com.fintech.customer.domain.Beneficiary;
import com.fintech.customer.dto.BeneficiaryResponse;
import com.fintech.customer.dto.BeneficiaryRequest;
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerOutboxRepository outboxRepository;
    private final OutboxMapper outboxMapper;
    private final MessageEnvelopeFactory messageEnvelopeFactory;
    private final AccountQueryAdapter accountQueryAdapter;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ProcessedMessagesRepository processedMessagesRepository;

    public CustomerService(CustomerRepository customerRepository, CustomerOutboxRepository outboxRepository, OutboxMapper outboxMapper, MessageEnvelopeFactory messageEnvelopeFactory, AccountQueryAdapter accountQueryAdapter, BeneficiaryRepository beneficiaryRepository, ProcessedMessagesRepository processedMessagesRepository){
        this.customerRepository = customerRepository;
        this.outboxRepository = outboxRepository;
        this.outboxMapper = outboxMapper;
        this.messageEnvelopeFactory = messageEnvelopeFactory;
        this.accountQueryAdapter = accountQueryAdapter;
        this.beneficiaryRepository = beneficiaryRepository;
        this.processedMessagesRepository = processedMessagesRepository;
    }

    @Transactional
    public CustomerStatusResponse createCustomer(CreateCustomerRequest createCustomerRequest){
        Customer customer = new Customer(createCustomerRequest.getFirstName(),
                createCustomerRequest.getLastName(),createCustomerRequest.getPhoneNumber());
        try {
            UUID customerId = this.customerRepository.saveAndFlush(customer).getId();
            CustomerCreatedEvent customerCreatedEvent =
                    new CustomerCreatedEvent(customerId.toString(),customer.getPhoneNumber()
                            , Instant.now());
//            String requestId = "customer"+"_"+UUID.randomUUID();
            String requestId = MDC.get("traceId");
            MessageEnvelope<CustomerCreatedEvent> messageEnvelope =
                    this.messageEnvelopeFactory
                            .build(requestId,
                                    "",
                                    "",
                                    "CustomerService",
                                    customerCreatedEvent);
            CustomerOutboxEvent outboxEvent =
                    outboxMapper.mapToOutboxEvent(
                            messageEnvelope,
                            CustomerOutboxEvent::new
                    );
            this.outboxRepository.save(outboxEvent);
            return new CustomerStatusResponse(customerId.toString(),
                    CustomerStatus.INITIATED.getStatus()
                    ,CustomerStatus.INITIATED.getStatusDetail());
        } catch (DataIntegrityViolationException dataIntegrityViolationException){
            throw new BaseException(CustomerErrorCode.DUPLICATE_CUSTOMER);
        } catch (Exception exception){
            throw new BaseException(CommonErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public CustomerStatusResponse fetchCustomerStatus(UUID customerId){
        try{
            Optional<Customer> customerOptional = this.customerRepository.findById(customerId);
             Customer customer = customerOptional
                    .orElseThrow(() -> new BaseException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
             String statusDetail = CustomerStatus.fetchDetailFromStatus(customer.getStatus());
             return new CustomerStatusResponse(customer.getId().toString()
                     ,customer.getStatus() , statusDetail);
        } catch (DataAccessException dataAccessException) {
            throw new BaseException(CommonErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public CustomerProfile getCustomerProfile(UUID customerId){
        CustomerProfile customerProfile = new CustomerProfile();
        try{
            Optional<Customer> customerOptional = this.customerRepository.findById(customerId);
            Customer customer = customerOptional
                    .orElseThrow(() -> new BaseException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
            CustomerAccountsResponse customerAccountsResponse =
                    this.accountQueryAdapter.fetchCustomerAccountDetails(customer.getId());
            customerProfile.setCustomerId(customer.getId().toString());
            customerProfile.setCustomerName(customer.getFirstName()+" "+customer.getLastName());
            customerProfile.setAccounts(customerAccountsResponse.getCustomerAccounts());
            AccountSummary defaultAccount = customerAccountsResponse.getCustomerAccounts().stream()
                    .filter(account-> account.isDefault()).findFirst().orElse(null);
            if(defaultAccount != null)
                customerProfile.setDefaultAccountId(defaultAccount.getAccountId());

            // load beneficiaries
            List<Beneficiary> beneficiaries = this.beneficiaryRepository.findByCustomerId(customer.getId());
            List<BeneficiaryResponse> beneficiaryResponses = beneficiaries.stream().map(b -> {
                BeneficiaryResponse resp = new BeneficiaryResponse();
                resp.setBeneficiaryId(b.getBeneficiaryId().toString());
                resp.setName(b.getName());
                resp.setAccountId(b.getAccountId());
                return resp;
            }).collect(Collectors.toList());
            customerProfile.setBeneficiaries(beneficiaryResponses);

        } catch (DataAccessException dataAccessException) {
            throw new BaseException(CommonErrorCode.INTERNAL_ERROR);
        }
        return customerProfile;
    }

    @Transactional
    public void updateCustomerStatus(UUID customerId, String status) {
        this.customerRepository.updateCustomerStatus(customerId,status,Instant.now());
    }

    @Transactional
    public BeneficiaryResponse addBeneficiary(UUID customerId, BeneficiaryRequest request) {
        // ensure customer exists
        Optional<Customer> customerOptional = this.customerRepository.findById(customerId);
        Customer customer = customerOptional
                .orElseThrow(() -> new BaseException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
        Beneficiary beneficiary = new Beneficiary(customer.getId(), request.getName(), request.getAccountId());
        beneficiary = this.beneficiaryRepository.saveAndFlush(beneficiary);
        BeneficiaryResponse resp = new BeneficiaryResponse();
        resp.setBeneficiaryId(beneficiary.getBeneficiaryId().toString());
        resp.setName(beneficiary.getName());
        resp.setAccountId(beneficiary.getAccountId());
        return resp;
    }

    @Transactional
    public void deleteBeneficiary(UUID customerId, UUID beneficiaryId) {
        // validate exists
        java.util.Optional<Beneficiary> beneficiaryOptional = this.beneficiaryRepository.findByBeneficiaryIdAndCustomerId(beneficiaryId, customerId);
        if (beneficiaryOptional.isEmpty()) {
            throw new BaseException(CustomerErrorCode.BENEFICIARY_NOT_FOUND);
        }
        this.beneficiaryRepository.deleteByBeneficiaryIdAndCustomerId(beneficiaryId, customerId);
    }

    @Transactional
    public int insertIntoProcessedMessages(String messageId){
        return processedMessagesRepository
                .insert(messageId, Instant.now());
    }
}

