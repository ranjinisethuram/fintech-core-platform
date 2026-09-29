package com.fintech.common.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER
}
