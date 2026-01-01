package com.mybank.models;

import java.sql.Timestamp;

public class ChequeBook {
    private int chequeBookId;
    private int accountId;
    private int customerId;
    private String bookNumber;
    private String startChequeNumber;
    private String endChequeNumber;
    private int totalLeaves;
    private int remainingLeaves;
    private Timestamp requestDate;
    private Timestamp approvalDate;
    private Integer approvedBy;
    private String status;
    private String rejectionReason;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional fields for display
    private String accountNumber;
    private String customerName;
    private String approvedByName;
    private String accountType;
    private double currentBalance;

    public ChequeBook() {
    }

    public ChequeBook(int accountId, int customerId, String bookNumber, 
                      String startChequeNumber, String endChequeNumber, int totalLeaves) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.bookNumber = bookNumber;
        this.startChequeNumber = startChequeNumber;
        this.endChequeNumber = endChequeNumber;
        this.totalLeaves = totalLeaves;
        this.remainingLeaves = totalLeaves;
        this.status = "PENDING";
    }

    // Getters and Setters
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

    public String getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(String bookNumber) {
        this.bookNumber = bookNumber;
    }

    public String getStartChequeNumber() {
        return startChequeNumber;
    }

    public void setStartChequeNumber(String startChequeNumber) {
        this.startChequeNumber = startChequeNumber;
    }

    public String getEndChequeNumber() {
        return endChequeNumber;
    }

    public void setEndChequeNumber(String endChequeNumber) {
        this.endChequeNumber = endChequeNumber;
    }

    public int getTotalLeaves() {
        return totalLeaves;
    }

    public void setTotalLeaves(int totalLeaves) {
        this.totalLeaves = totalLeaves;
    }

    public int getRemainingLeaves() {
        return remainingLeaves;
    }

    public void setRemainingLeaves(int remainingLeaves) {
        this.remainingLeaves = remainingLeaves;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }

    public Timestamp getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Timestamp approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }
}
