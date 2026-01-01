package com.mybank.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a customer account opening request.
 * Used for managing pending account applications that require staff approval.
 */
public class AccountRequest {
    private int requestId;
    private String customerName;
    private String email;
    private String phoneNumber;
    private String address;
    private String identityType;
    private String identityNumber;
    private String accountType;
    private double initialDeposit;
    private String requestStatus;
    private LocalDateTime requestDate;
    private Integer processedBy;
    private LocalDateTime processedDate;
    private String remarks;
    private Integer accountNumber;
    private String profilePicturePath;
    
    // Request Status Constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    
    // Identity Types
    public static final String ID_NATIONAL = "NATIONAL_ID";
    public static final String ID_PASSPORT = "PASSPORT";
    public static final String ID_DRIVING_LICENSE = "DRIVING_LICENSE";
    
    // Constructors
    public AccountRequest() {}
    
    public AccountRequest(String customerName, String email, String phoneNumber, 
                         String address, String identityType, String identityNumber, 
                         String accountType, double initialDeposit) {
        this.customerName = customerName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.identityType = identityType;
        this.identityNumber = identityNumber;
        this.accountType = accountType;
        this.initialDeposit = initialDeposit;
        this.requestStatus = STATUS_PENDING;
    }
    
    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }
    
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getIdentityType() {
        return identityType;
    }
    
    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }
    
    public String getIdentityNumber() {
        return identityNumber;
    }
    
    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public double getInitialDeposit() {
        return initialDeposit;
    }
    
    public void setInitialDeposit(double initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
    
    public String getRequestStatus() {
        return requestStatus;
    }
    
    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
    
    public LocalDateTime getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
    
    public Integer getProcessedBy() {
        return processedBy;
    }
    
    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }
    
    public LocalDateTime getProcessedDate() {
        return processedDate;
    }
    
    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public Integer getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(Integer accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getProfilePicturePath() {
        return profilePicturePath;
    }
    
    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
    
    // Utility Methods
    
    /**
     * Check if request is pending
     */
    public boolean isPending() {
        return STATUS_PENDING.equals(requestStatus);
    }
    
    /**
     * Check if request is approved
     */
    public boolean isApproved() {
        return STATUS_APPROVED.equals(requestStatus);
    }
    
    /**
     * Check if request is rejected
     */
    public boolean isRejected() {
        return STATUS_REJECTED.equals(requestStatus);
    }
    
    /**
     * Check if request is being processed
     */
    public boolean isProcessing() {
        return STATUS_PROCESSING.equals(requestStatus);
    }
    
    /**
     * Get formatted request date
     */
    public String getFormattedRequestDate() {
        if (requestDate == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        return requestDate.format(formatter);
    }
    
    /**
     * Get formatted processed date
     */
    public String getFormattedProcessedDate() {
        if (processedDate == null) return "Not Processed";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        return processedDate.format(formatter);
    }
    
    /**
     * Get formatted initial deposit with TAKA currency
     */
    public String getFormattedInitialDeposit() {
        return String.format("%.2f TAKA", initialDeposit);
    }
    
    /**
     * Get identity type display name
     */
    public String getIdentityTypeDisplay() {
        switch (identityType) {
            case ID_NATIONAL:
                return "National ID";
            case ID_PASSPORT:
                return "Passport";
            case ID_DRIVING_LICENSE:
                return "Driving License";
            default:
                return identityType;
        }
    }
    
    /**
     * Get account type display name
     */
    public String getAccountTypeDisplay() {
        switch (accountType.toUpperCase()) {
            case "SAVINGS":
                return "Savings Account";
            case "CURRENT":
                return "Current Account";
            case "LOAN":
                return "Loan Account";
            default:
                return accountType;
        }
    }
    
    /**
     * Get masked identity number for privacy (last 4 digits)
     */
    public String getMaskedIdentityNumber() {
        if (identityNumber == null || identityNumber.length() <= 4) {
            return identityNumber;
        }
        return "****" + identityNumber.substring(identityNumber.length() - 4);
    }
    
    @Override
    public String toString() {
        return "AccountRequest{" +
                "requestId=" + requestId +
                ", customerName='" + customerName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", requestStatus='" + requestStatus + '\'' +
                ", requestDate=" + requestDate +
                '}';
    }
}
