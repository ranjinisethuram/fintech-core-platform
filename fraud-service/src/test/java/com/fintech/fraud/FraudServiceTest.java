package com.fintech.fraud;

import com.fintech.fraud.dto.FraudEvaluationRequest;
import com.fintech.fraud.dto.FraudEvaluationResponse;
import com.fintech.fraud.rule.RiskLevel;
import com.fintech.fraud.service.FraudService;
import com.fintech.fraudcontract.dto.HistoricalTransaction;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.fraud.outbox.FraudOutboxRepository;
import com.fintech.outbox.OutboxMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FraudServiceTest {

    @Mock
    private MessageEnvelopeFactory envelopeFactory;

    @Mock
    private FraudOutboxRepository fraudOutboxRepository;

    @Mock
    private OutboxMapper outboxMapper;

    private FraudService fraudService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fraudService = new FraudService(envelopeFactory, fraudOutboxRepository, outboxMapper);
    }

    @Test
    void shouldReturnCriticalRiskForHighRiskTransaction() {
        Instant baseTime = Instant.parse("2026-01-10T10:00:00Z");
        List<HistoricalTransaction> history = List.of(
                new HistoricalTransaction(new BigDecimal("20000"), baseTime.minusSeconds(120)),
                new HistoricalTransaction(new BigDecimal("25000"), baseTime.minusSeconds(180)),
                new HistoricalTransaction(new BigDecimal("22000"), baseTime.minusSeconds(240)),
                new HistoricalTransaction(new BigDecimal("23000"), baseTime.minusSeconds(300)),
                new HistoricalTransaction(new BigDecimal("21000"), baseTime.minusSeconds(360))
        );

        FraudEvaluationRequest request = new FraudEvaluationRequest(
                new BigDecimal("150000"),
                baseTime,
                history
        );

        FraudEvaluationResponse response = fraudService.evaluate(request);

        assertEquals(90, response.totalScore());
        assertEquals(RiskLevel.CRITICAL, response.riskLevel());
    }

    @Test
    void shouldReturnLowRiskForSafeTransaction() {
        FraudEvaluationRequest request = new FraudEvaluationRequest(
                new BigDecimal("5000"),
                Instant.parse("2026-01-10T10:00:00Z"),
                List.of(
                        new HistoricalTransaction(new BigDecimal("2000"), Instant.parse("2026-01-10T09:55:00Z")),
                        new HistoricalTransaction(new BigDecimal("3000"), Instant.parse("2026-01-10T09:50:00Z"))
                )
        );

        FraudEvaluationResponse response = fraudService.evaluate(request);

        assertEquals(0, response.totalScore());
        assertEquals(RiskLevel.LOW, response.riskLevel());
    }
}
