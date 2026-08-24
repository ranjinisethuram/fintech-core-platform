package com.fintech.fraud.dto;

import com.fintech.fraud.rule.RiskLevel;

import java.util.List;

public record FraudEvaluationResponse(int totalScore, RiskLevel riskLevel, List<RuleEvaluationResult> ruleResults) {
}
