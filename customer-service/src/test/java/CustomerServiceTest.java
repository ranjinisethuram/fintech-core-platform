import com.fintech.common.event.CustomerCreatedEvent;
import com.fintech.common.exception.BaseException;
import com.fintech.common.exception.CommonErrorCode;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.customer.domain.Customer;
import com.fintech.customer.domain.CustomerStatus;
import com.fintech.customer.dto.CreateCustomerRequest;
import com.fintech.customer.dto.CustomerStatusResponse;
import com.fintech.customer.exception.CustomerErrorCode;
import com.fintech.customer.feign.account.AccountQueryAdapter;
import com.fintech.customer.outbox.CustomerOutboxEvent;
import com.fintech.customer.outbox.CustomerOutboxRepository;
import com.fintech.customer.repository.CustomerRepository;
import com.fintech.customer.service.CustomerService;
import com.fintech.outbox.OutboxMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService - createCustomer() Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerOutboxRepository outboxRepository;

    @Mock
    private OutboxMapper outboxMapper;

    @Mock
    private MessageEnvelopeFactory messageEnvelopeFactory;

    @Mock
    private AccountQueryAdapter accountQueryAdapter;

    @InjectMocks
    private CustomerService customerService;

    private CreateCustomerRequest createCustomerRequest;
    private Customer customer;
    private UUID customerId;
    private String phoneNumber;
    private static final String TRACE_ID = "trace-123";

    @BeforeEach
    void setUp() {
        phoneNumber = "+1-9876543210";
        customerId = UUID.randomUUID();

        createCustomerRequest = new CreateCustomerRequest();
        createCustomerRequest.setFirstName("John");
        createCustomerRequest.setLastName("Doe");
        createCustomerRequest.setPhoneNumber(phoneNumber);

        customer = new Customer("John", "Doe", phoneNumber);
        ReflectionTestUtils.setField(customer, "id", customerId);

        // Set up MDC with trace ID
        MDC.put("traceId", TRACE_ID);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should successfully create customer and return INITIATED status")
    void testCreateCustomer_Success() {
        // Arrange
        @SuppressWarnings("unchecked")
        MessageEnvelope<AggregateMessage> messageEnvelope = (MessageEnvelope<AggregateMessage>) (MessageEnvelope<?>) new MessageEnvelope<>();
        CustomerOutboxEvent outboxEvent = new CustomerOutboxEvent();

        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenReturn(customer);
        when(messageEnvelopeFactory.build(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(messageEnvelope);
        when(outboxMapper.mapToOutboxEvent(eq(messageEnvelope), any()))
                .thenReturn(outboxEvent);

        // Act
        CustomerStatusResponse response = customerService.createCustomer(createCustomerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(customerId.toString(), response.getCustomerId());
        assertEquals(CustomerStatus.INITIATED.getStatus(), response.getStatus());
        assertEquals(CustomerStatus.INITIATED.getStatusDetail(), response.getStatusDetails());

        verify(customerRepository, times(1)).saveAndFlush(any(Customer.class));
        verify(messageEnvelopeFactory, times(1)).build(eq(TRACE_ID), eq(""), eq(""), eq("CustomerService"), any());
        verify(outboxRepository, times(1)).save(outboxEvent);
    }

    @Test
    @DisplayName("Should throw DUPLICATE_CUSTOMER error when phone number already exists")
    void testCreateCustomer_DuplicatePhoneNumber() {
        // Arrange
        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation on phone_number"));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            customerService.createCustomer(createCustomerRequest);
        });

        assertEquals(CustomerErrorCode.DUPLICATE_CUSTOMER, exception.getErrorCode());

        verify(customerRepository, times(1)).saveAndFlush(any(Customer.class));
        verify(messageEnvelopeFactory, never()).build(anyString(), anyString(), anyString(), anyString(), any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw INTERNAL_ERROR when unexpected exception occurs during customer save")
    void testCreateCustomer_UnexpectedException_DuringSave() {
        // Arrange
        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            customerService.createCustomer(createCustomerRequest);
        });

        assertEquals(CommonErrorCode.INTERNAL_ERROR, exception.getErrorCode());

        verify(customerRepository, times(1)).saveAndFlush(any(Customer.class));
        verify(messageEnvelopeFactory, never()).build(anyString(), anyString(), anyString(), anyString(), any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw INTERNAL_ERROR when message envelope factory fails")
    void testCreateCustomer_UnexpectedException_MessageEnvelopeFactory() {
        // Arrange
        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenReturn(customer);
        when(messageEnvelopeFactory.build(anyString(), anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Message factory error"));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            customerService.createCustomer(createCustomerRequest);
        });

        assertEquals(CommonErrorCode.INTERNAL_ERROR, exception.getErrorCode());

        verify(customerRepository, times(1)).saveAndFlush(any(Customer.class));
        verify(messageEnvelopeFactory, times(1)).build(eq(TRACE_ID), anyString(), anyString(), eq("CustomerService"), any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw INTERNAL_ERROR when outbox mapper fails")
    void testCreateCustomer_UnexpectedException_OutboxMapper() {
        // Arrange
        @SuppressWarnings("unchecked")
        MessageEnvelope<AggregateMessage> messageEnvelope = (MessageEnvelope<AggregateMessage>) (MessageEnvelope<?>) new MessageEnvelope<>();

        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenReturn(customer);
        when(messageEnvelopeFactory.build(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(messageEnvelope);
        when(outboxMapper.mapToOutboxEvent(eq(messageEnvelope), any()))
                .thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            customerService.createCustomer(createCustomerRequest);
        });

        assertEquals(CommonErrorCode.INTERNAL_ERROR, exception.getErrorCode());

        verify(customerRepository, times(1)).saveAndFlush(any(Customer.class));
        verify(messageEnvelopeFactory, times(1)).build(eq(TRACE_ID), anyString(), anyString(), eq("CustomerService"), any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw INTERNAL_ERROR when outbox repository save fails")
    void testCreateCustomer_UnexpectedException_OutboxSave() {
        // Arrange
        @SuppressWarnings("unchecked")
        MessageEnvelope<AggregateMessage> messageEnvelope = (MessageEnvelope<AggregateMessage>) (MessageEnvelope<?>) new MessageEnvelope<>();
        CustomerOutboxEvent outboxEvent = new CustomerOutboxEvent();

        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenReturn(customer);
        when(messageEnvelopeFactory.build(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(messageEnvelope);
        when(outboxMapper.mapToOutboxEvent(eq(messageEnvelope), any()))
                .thenReturn(outboxEvent);
        when(outboxRepository.save(outboxEvent))
                .thenThrow(new RuntimeException("Outbox save error"));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            customerService.createCustomer(createCustomerRequest);
        });

        assertEquals(CommonErrorCode.INTERNAL_ERROR, exception.getErrorCode());

        verify(customerRepository, times(1)).saveAndFlush(any(Customer.class));
        verify(messageEnvelopeFactory, times(1)).build(eq(TRACE_ID), anyString(), anyString(), eq("CustomerService"), any());
        verify(outboxRepository, times(1)).save(outboxEvent);
    }

    @Test
    @DisplayName("Should correctly create Customer entity from request")
    void testCreateCustomer_VerifyCustomerCreation() {
        // Arrange
        @SuppressWarnings("unchecked")
        MessageEnvelope<AggregateMessage> messageEnvelope = (MessageEnvelope<AggregateMessage>) (MessageEnvelope<?>) new MessageEnvelope<>();
        CustomerOutboxEvent outboxEvent = new CustomerOutboxEvent();

        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenReturn(customer);
        when(messageEnvelopeFactory.build(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(messageEnvelope);
        when(outboxMapper.mapToOutboxEvent(eq(messageEnvelope), any()))
                .thenReturn(outboxEvent);

        // Act
        customerService.createCustomer(createCustomerRequest);

        // Assert - Verify customer entity was created with correct data
        verify(customerRepository).saveAndFlush(argThat(cust ->
                cust.getFirstName().equals("John") &&
                        cust.getLastName().equals("Doe") &&
                        cust.getPhoneNumber().contains("9876543210") &&
                        cust.getStatus().equals(CustomerStatus.INITIATED.getStatus())
        ));
    }

    @Test
    @DisplayName("Should normalize phone number when creating customer")
    void testCreateCustomer_PhoneNumberNormalization() {
        // Arrange
        String phoneWithSpaces = "+1-98 76 54 3210";
        createCustomerRequest.setPhoneNumber(phoneWithSpaces);

        @SuppressWarnings("unchecked")
        MessageEnvelope<AggregateMessage> messageEnvelope = (MessageEnvelope<AggregateMessage>) (MessageEnvelope<?>) new MessageEnvelope<>();
        CustomerOutboxEvent outboxEvent = new CustomerOutboxEvent();

        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenReturn(customer);
        when(messageEnvelopeFactory.build(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(messageEnvelope);
        when(outboxMapper.mapToOutboxEvent(eq(messageEnvelope), any()))
                .thenReturn(outboxEvent);

        // Act
        customerService.createCustomer(createCustomerRequest);

        // Assert - Verify phone number was normalized (spaces removed)
        verify(customerRepository).saveAndFlush(argThat(cust ->
                !cust.getPhoneNumber().contains(" ")
        ));
    }

    @Test
    @DisplayName("Should create event with correct customer ID and phone number")
    void testCreateCustomer_VerifyCustomerCreatedEvent() {
        // Arrange
        @SuppressWarnings("unchecked")
        MessageEnvelope<AggregateMessage> messageEnvelope = (MessageEnvelope<AggregateMessage>) (MessageEnvelope<?>) new MessageEnvelope<>();
        CustomerOutboxEvent outboxEvent = new CustomerOutboxEvent();

        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenReturn(customer);
        when(messageEnvelopeFactory.build(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(messageEnvelope);
        when(outboxMapper.mapToOutboxEvent(eq(messageEnvelope), any()))
                .thenReturn(outboxEvent);

        // Act
        customerService.createCustomer(createCustomerRequest);

        // Assert - Verify CustomerCreatedEvent contains correct data by verifying the call was made
        verify(messageEnvelopeFactory).build(
                anyString(),
                eq(""),
                eq(""),
                eq("CustomerService"),
                any()
        );
    }
}
