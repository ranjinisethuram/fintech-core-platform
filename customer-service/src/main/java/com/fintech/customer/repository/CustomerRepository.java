package com.fintech.customer.repository;

import com.fintech.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Modifying
    @Query(
            value = """
            UPDATE customers
            SET status = :status,
            updated_at = :updated_at
            WHERE id = :id
            """,
            nativeQuery = true
    )
    public void updateCustomerStatus(@Param("id") UUID id, @Param("status") String status
            , @Param("updated_at") Instant updatedAt);
}
