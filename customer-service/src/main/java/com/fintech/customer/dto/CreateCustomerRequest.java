package com.fintech.customer.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class CreateCustomerRequest {

    @NotEmpty(message = "First Name is required.")
    String firstName;
    @NotEmpty(message = "Last Name is required.")
    String lastName;
    @NotEmpty(message = "Phone Number is not valid.")
    @Pattern(regexp = "^\\+\\d{1,4}-?\\d+$")
    private String phoneNumber;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
