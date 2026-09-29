package com.fintech.customer.repository;

import com.fintech.customer.domain.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    List<Beneficiary> findByCustomerId(UUID customerId);
    Optional<Beneficiary> findByBeneficiaryIdAndCustomerId(UUID beneficiaryId, UUID customerId);
    void deleteByBeneficiaryIdAndCustomerId(UUID beneficiaryId, UUID customerId);
}
