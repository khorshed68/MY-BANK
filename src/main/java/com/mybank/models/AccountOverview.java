package com.mybank.models;

/**
 * Account Overview Model
 * Represents comprehensive account information for display in overview dashboard
 */
public class AccountOverview {
    
    private int accountNumber;
    private String ownerName;
    private String accountType;        // SAVINGS, CURRENT, LOAN
    private double balance;
    private double availableBalance;
    private String status;             // ACTIVE, BLOCKED, CLOSED
    private String lastTransactionDate;
    private String createdDate;
    private String email;
    private String phoneNumber;
    
    // Constructors
    public AccountOverview() {
    }
    
    public AccountOverview(int accountNumber, String ownerName, String accountType, 
                          double balance, double availableBalance, String status,
                          String lastTransactionDate, String createdDate) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.accountType = accountType;
        this.balance = balance;
        this.availableBalance = availableBalance;
        this.status = status;
        this.lastTransactionDate = lastTransactionDate;
        this.createdDate = createdDate;
    }
    
    /**
     * Returns masked account number for security (shows only last 4 digits)
     * @return Masked account number (e.g., ****1234)
     */
    public String getMaskedAccountNumber() {
        String accNumStr = String.valueOf(accountNumber);
        if (accNumStr.length() <= 4) {
            return accNumStr;
        }
        return "****" + accNumStr.substring(accNumStr.length() - 4);
    }
    
    /**
     * Returns full account number (use only when necessary for operations)
     * @return Full account number
     */
    public int getAccountNumber() {
        return accountNumber;
    }
    
    /**
     * Returns account type display string
     * @return Formatted account type
     */
    public String getAccountTypeDisplay() {
        switch (accountType) {
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
     * Returns status display string with color indication
     * @return Formatted status
     */
    public String getStatusDisplay() {
        return status != null ? status : "UNKNOWN";
    }
    
    /**
     * Checks if account is active
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    /**
     * Checks if account is blocked
     * @return true if blocked, false otherwise
     */
    public boolean isBlocked() {
        return "BLOCKED".equals(status);
    }
    
    /**
     * Checks if account is closed
     * @return true if closed, false otherwise
     */
    public boolean isClosed() {
        return "CLOSED".equals(status);
    }
    
    /**
     * Returns formatted balance with currency
     * @return Formatted balance string
     */
    public String getFormattedBalance() {
        return String.format("%.2f TAKA", balance);
    }
    
    /**
     * Returns formatted available balance with currency
     * @return Formatted available balance string
     */
    public String getFormattedAvailableBalance() {
        return String.format("%.2f TAKA", availableBalance);
    }
    
    // Getters and Setters
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public double getAvailableBalance() {
        return availableBalance;
    }
    
    public void setAvailableBalance(double availableBalance) {
        this.availableBalance = availableBalance;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLastTransactionDate() {
        return lastTransactionDate != null ? lastTransactionDate : "No transactions yet";
    }
    
    public void setLastTransactionDate(String lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
    
    public String getCreatedDate() {
        return createdDate != null ? createdDate : "Unknown";
    }
    
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
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
    
    @Override
    public String toString() {
        return "AccountOverview{" +
                "accountNumber=" + getMaskedAccountNumber() +
                ", ownerName='" + ownerName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", status='" + status + '\'' +
                '}';
    }
}
