package com.fintech.customer.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "customers",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "customer_phone",
            columnNames = "phone_number"
        )
    }
)
public class Customer {

    @Id
    private UUID id;
    @Column(name="first_name", nullable = false)
    private String firstName;
    @Column(name="last_name", nullable = false)
    private String lastName;
    @Column(name="phone_number",nullable = false, length = 20)
    private String phoneNumber;
    @Column(name="status",nullable = false)
    private String status;
    @Column(name="created_at",nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name="updated_at", nullable = false)
    private Instant updatedAt;

    protected Customer(){
    }

    public Customer (String firstname, String lastName, String phoneNumber){
        this.id = UUID.randomUUID();
        this.firstName = firstname;
        this.lastName = lastName;
        this.phoneNumber = normalize(phoneNumber);
        this.status = CustomerStatus.INITIATED.getStatus();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    private String normalize(String phoneNumber){
        return phoneNumber.replaceAll("\\s+","");
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
