package com.mybank.models;

/**
 * Account model class
 * Represents a bank account
 */
public class Account {
    private int accountNumber;
    private String ownerName;
    private double balance;
    private String email;
    private String phoneNumber;
    private String accountType;
    private String status;
    private String createdDate;
    private String profilePicturePath;
    
    /**
     * Default Constructor
     */
    public Account() {
    }
    
    /**
     * Constructor
     * @param accountNumber The account number
     * @param ownerName The owner's name
     * @param balance The account balance
     */
    public Account(int accountNumber, String ownerName, double balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }
    
    // Getters and Setters
    public int getAccountNumber() {
        return accountNumber;
    }
    
    public String getAccountNumberString() {
        return String.valueOf(accountNumber);
    }
    
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public String getCustomerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public void setCustomerName(String customerName) {
        this.ownerName = customerName;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
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
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getAccountStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setAccountStatus(String status) {
        this.status = status;
    }
    
    public String getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getProfilePicturePath() {
        return profilePicturePath;
    }
    
    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "accountNumber=" + accountNumber +
                ", ownerName='" + ownerName + '\'' +
                ", balance=" + balance +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", accountType='" + accountType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
