package com.fintech.wallet.repository;

import com.fintech.wallet.domain.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ProcessedMessagesRepository extends JpaRepository<ProcessedMessage,String> {

    @Modifying
    @Query(value = """
            INSERT INTO processed_messages (message_id,processed_at)
            VALUES (:message_id,:processed_at)
            ON CONFLICT (message_id) DO NOTHING;
            """,
            nativeQuery = true)
    int insert(@Param("message_id") String messageId, @Param("processed_at") Instant processedA);

}
