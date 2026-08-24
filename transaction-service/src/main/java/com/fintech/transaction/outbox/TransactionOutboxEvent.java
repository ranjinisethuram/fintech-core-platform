package com.fintech.transaction.outbox;

import com.fintech.outbox.BaseOutboxEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="transaction_outbox",
        indexes = {
                @Index(
                        name = "idx_transaction_outbox_status_created_at_retry",
                        columnList = "status, retry_at, created_at"
                )
        }
)
public class TransactionOutboxEvent extends BaseOutboxEvent {
}
