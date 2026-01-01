package com.mybank.models;

/**
 * Account Information Model
 * Holds complete account details including contact information
 */
public class AccountInfo {
    private int accountNumber;
    private String ownerName;
    private double balance;
    private String email;
    private String phoneNumber;
    private boolean smsEnabled;
    private boolean emailEnabled;
    private String status;
    private String accountType;
    
    public AccountInfo() {
    }
    
    public AccountInfo(int accountNumber, String ownerName, double balance, 
                      String email, String phoneNumber, boolean smsEnabled, boolean emailEnabled, 
                      String status, String accountType) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.smsEnabled = smsEnabled;
        this.emailEnabled = emailEnabled;
        this.status = status;
        this.accountType = accountType;
    }
    
    // Getters and Setters
    public int getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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
    
    public boolean isSmsEnabled() {
        return smsEnabled;
    }
    
    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }
    
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
    
    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    /**
     * Returns a masked account number for security (e.g., ****1234)
     */
    public String getMaskedAccountNumber() {
        String accStr = String.valueOf(accountNumber);
        if (accStr.length() <= 4) {
            return accStr;
        }
        int visibleDigits = 4;
        String masked = "****" + accStr.substring(accStr.length() - visibleDigits);
        return masked;
    }
}
