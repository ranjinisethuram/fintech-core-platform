package com.fintech.orchestration.outbox;

import com.fintech.outbox.BaseOutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="saga_outbox",
        indexes = {
                @Index(
                        name = "idx_saga_outbox_status_created_at_retry",
                        columnList = "status, retry_at, created_at"
                )
        }
)
public class SagaOutbox extends BaseOutboxEvent {

    @Column(name = "topic_name", nullable = false, updatable = false)
    private String topicName;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
