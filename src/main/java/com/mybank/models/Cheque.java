package com.mybank.models;

import java.sql.Date;
import java.sql.Timestamp;

public class Cheque {
    private int chequeId;
    private int chequeBookId;
    private int accountId;
    private int customerId;
    private String chequeNumber;
    private double amount;
    private String payeeName;
    private Date issueDate;
    private Timestamp depositDate;
    private Integer depositedByAccount;
    private Integer depositedByCustomer;
    private Timestamp clearanceDate;
    private String status;
    private String bounceReason;
    private Integer processedBy;
    private boolean signatureVerified;
    private String remarks;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional fields for display
    private String accountNumber;
    private String customerName;
    private String bookNumber;
    private double currentBalance;
    private String depositedByName;
    private String depositedToAccount;

    public Cheque() {
    }

    public Cheque(int chequeBookId, int accountId, int customerId, String chequeNumber) {
        this.chequeBookId = chequeBookId;
        this.accountId = accountId;
        this.customerId = customerId;
        this.chequeNumber = chequeNumber;
        this.status = "ISSUED";
        this.signatureVerified = false;
    }

    // Getters and Setters
    public int getChequeId() {
        return chequeId;
    }

    public void setChequeId(int chequeId) {
        this.chequeId = chequeId;
    }

    public int getChequeBookId() {
        return chequeBookId;
    }

    public void setChequeBookId(int chequeBookId) {
        this.chequeBookId = chequeBookId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Timestamp getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(Timestamp depositDate) {
        this.depositDate = depositDate;
    }

    public Integer getDepositedByAccount() {
        return depositedByAccount;
    }

    public void setDepositedByAccount(Integer depositedByAccount) {
        this.depositedByAccount = depositedByAccount;
    }

    public Integer getDepositedByCustomer() {
        return depositedByCustomer;
    }

    public void setDepositedByCustomer(Integer depositedByCustomer) {
        this.depositedByCustomer = depositedByCustomer;
    }

    public Timestamp getClearanceDate() {
        return clearanceDate;
    }

    public void setClearanceDate(Timestamp clearanceDate) {
        this.clearanceDate = clearanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBounceReason() {
        return bounceReason;
    }

    public void setBounceReason(String bounceReason) {
        this.bounceReason = bounceReason;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }

    public boolean isSignatureVerified() {
        return signatureVerified;
    }

    public void setSignatureVerified(boolean signatureVerified) {
        this.signatureVerified = signatureVerified;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(String bookNumber) {
        this.bookNumber = bookNumber;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getDepositedByName() {
        return depositedByName;
    }

    public void setDepositedByName(String depositedByName) {
        this.depositedByName = depositedByName;
    }

    public String getDepositedToAccount() {
        return depositedToAccount;
    }

    public void setDepositedToAccount(String depositedToAccount) {
        this.depositedToAccount = depositedToAccount;
    }
}
