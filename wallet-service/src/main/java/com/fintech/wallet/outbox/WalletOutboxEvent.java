package com.fintech.wallet.outbox;

import com.fintech.outbox.BaseOutboxEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="wallet_outbox",
        indexes = {
                @Index(
                        name = "idx_wallet_outbox_status_created_at_retry",
                        columnList = "status, retry_at, created_at"
                )
        }
)
public class WalletOutboxEvent extends BaseOutboxEvent {
}
