package com.fintech.common.messaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
public interface AggregateMessage {

    @JsonIgnore // Stops Jackson from trying to serialize this into the DB JSON
    String getAggregateId();
    @JsonIgnore // Stops Jackson from trying to serialize this into the DB JSON
    AggregateType getAggregateType();
}
