package com.fintech.customer.outbox;

import com.fintech.outbox.BaseOutboxEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="customer_outbox",
        indexes = {
                @Index(
                        name = "idx_customer_outbox_status_created_at_retry",
                        columnList = "status, retry_at, created_at"
                )
        }
)
public class CustomerOutboxEvent extends BaseOutboxEvent {

}
