package com.fintech.fraud.service;

import com.fintech.common.command.EvaluateFraudCommand;
import com.fintech.common.event.FraudEvaluatedEvent;
import com.fintech.common.event.FraudEvaluationFailedEvent;
import com.fintech.common.exception.BaseException;
import com.fintech.common.messaging.AggregateMessage;
import com.fintech.common.messaging.MessageEnvelope;
import com.fintech.common.messaging.MessageEnvelopeFactory;
import com.fintech.fraud.dto.FraudEvaluationRequest;
import com.fintech.fraud.dto.FraudEvaluationResponse;
import com.fintech.fraud.dto.RuleEvaluationResult;
import com.fintech.fraud.exception.FraudErrorCode;
import com.fintech.fraud.outbox.FraudOutboxEvent;
import com.fintech.fraud.outbox.FraudOutboxRepository;
import com.fintech.fraud.rule.HighAmountRule;
import com.fintech.fraud.rule.HighVelocityRule;
import com.fintech.fraud.rule.RiskLevel;
import com.fintech.fraud.rule.UnusualAmountRule;
import com.fintech.fraudcontract.dto.HistoricalTransaction;
import com.fintech.outbox.OutboxMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudService {

    private final HighAmountRule highAmountRule = new HighAmountRule();
    private final HighVelocityRule highVelocityRule = new HighVelocityRule();
    private final UnusualAmountRule unusualAmountRule = new UnusualAmountRule();
    private final MessageEnvelopeFactory messageEnvelopeFactory;
    private final FraudOutboxRepository fraudOutboxRepository;
    private final OutboxMapper outboxMapper;

    public FraudService(MessageEnvelopeFactory messageEnvelopeFactory,
                        FraudOutboxRepository fraudOutboxRepository,
                        OutboxMapper outboxMapper) {
        this.messageEnvelopeFactory = messageEnvelopeFactory;
        this.fraudOutboxRepository = fraudOutboxRepository;
        this.outboxMapper = outboxMapper;
    }

    public FraudEvaluationResponse evaluate(FraudEvaluationRequest request) {
        if (request == null) {
            throw new BaseException(FraudErrorCode.INVALID_TRANSACTION_DATA);
        }

        BigDecimal amount = request.amount();
        List<HistoricalTransaction> historicalTransactions = request.historicalTransactions();
        List<RuleEvaluationResult> ruleResults = new ArrayList<>();
        int totalScore = 0;

        int highAmountScore = highAmountRule.evaluate(amount);
        if (highAmountScore > 0) {
            ruleResults.add(new RuleEvaluationResult(highAmountRule.getName(), highAmountScore));
        }
        totalScore += highAmountScore;

        int velocityScore = highVelocityRule.evaluate(historicalTransactions, request.transactionTimestamp(), amount);
        if (velocityScore > 0) {
            ruleResults.add(new RuleEvaluationResult(highVelocityRule.getName(), velocityScore));
        }
        totalScore += velocityScore;

        int unusualAmountScore = unusualAmountRule.evaluate(amount, historicalTransactions);
        if (unusualAmountScore > 0) {
            ruleResults.add(new RuleEvaluationResult(unusualAmountRule.getName(), unusualAmountScore));
        }
        totalScore += unusualAmountScore;

        return new FraudEvaluationResponse(totalScore, RiskLevel.fromScore(totalScore), ruleResults);
    }

    public FraudEvaluationResponse evaluate(EvaluateFraudCommand command) {
        if (command == null) {
            throw new BaseException(FraudErrorCode.INVALID_TRANSACTION_DATA);
        }

        List<HistoricalTransaction> history = command.historicalTransactions() == null ? List.of() : command.historicalTransactions().stream()
                .map(item -> new HistoricalTransaction(item.amount(), item.timestamp()))
                .toList();

        FraudEvaluationRequest request = new FraudEvaluationRequest(
                command.amount(),
                command.transactionTimestamp() == null ? Instant.now() : command.transactionTimestamp(),
                history
        );
        return evaluate(request);
    }

    @Transactional
    public void evaluateCommand(EvaluateFraudCommand command, String correlationId, String causationId, String sagaId) {
        try {
            FraudEvaluationResponse response = evaluate(command);
            FraudEvaluatedEvent event = new FraudEvaluatedEvent(
                    command.transactionId().toString(),
                    response.totalScore(),
                    response.riskLevel().name(),
                    response.ruleResults().stream().map(RuleEvaluationResult::ruleName).toList(),
                    Instant.now()
            );
            buildMessageEnvelopeAndPushToOutbox(event, correlationId, causationId, sagaId);
        } catch (BaseException exception) {
            FraudEvaluationFailedEvent failedEvent = new FraudEvaluationFailedEvent(
                    command.transactionId().toString(),
                    exception.getErrorCode().getErrorCode(),
                    exception.getErrorCode().getErrorMessage(),
                    exception.getErrorCode().isRetryable(),
                    Instant.now()
            );
            buildMessageEnvelopeAndPushToOutbox(failedEvent, correlationId, causationId, sagaId);
        } catch (Exception exception) {
            FraudEvaluationFailedEvent failedEvent = new FraudEvaluationFailedEvent(
                    command.transactionId().toString(),
                    FraudErrorCode.FRAUD_EVALUATION_FAILED.getErrorCode(),
                    exception.getMessage() == null ? FraudErrorCode.FRAUD_EVALUATION_FAILED.getErrorMessage() : exception.getMessage(),
                    FraudErrorCode.FRAUD_EVALUATION_FAILED.isRetryable(),
                    Instant.now()
            );
            buildMessageEnvelopeAndPushToOutbox(failedEvent, correlationId, causationId, sagaId);
        }
    }

    private <T extends AggregateMessage> void buildMessageEnvelopeAndPushToOutbox(T eventMessage,
              String correlationId,
              String causationId,
              String sagaId) {
        MessageEnvelope<T> messageEnvelope = this.messageEnvelopeFactory
                .build(correlationId, causationId, sagaId, "fraud-service", eventMessage);
        FraudOutboxEvent fraudOutboxEvent = outboxMapper.mapToOutboxEvent(messageEnvelope, FraudOutboxEvent::new);
        this.fraudOutboxRepository.saveAndFlush(fraudOutboxEvent);
    }

    public <T extends AggregateMessage> void writeFailedEventMessageToOutbox(T eventMessage,
             String correlationId,
             String causationId,
             String sagaId) {
        buildMessageEnvelopeAndPushToOutbox(eventMessage, correlationId, causationId, sagaId);
    }
}
