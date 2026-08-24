package com.fintech.account.outbox;

import com.fintech.outbox.BaseOutboxEvent;
import com.fintech.outbox.OutboxStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="account_outbox",
        indexes = {
                @Index(
                        name = "idx_account_outbox_status_created_at_retry",
                        columnList = "status, retry_at, created_at"
                )
        }
)
public class AccountOutboxEvent extends BaseOutboxEvent {

}
