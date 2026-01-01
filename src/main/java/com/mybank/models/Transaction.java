package com.mybank.models;

/**
 * Transaction model class
 * Represents a bank transaction
 */
public class Transaction {
    private int id;
    private int accountNumber;
    private String type;
    private double amount;
    private String date;
    
    /**
     * Constructor
     * @param id Transaction ID
     * @param accountNumber Account number
     * @param type Transaction type
     * @param amount Transaction amount
     * @param date Transaction date
     */
    public Transaction(int id, int accountNumber, String type, double amount, String date) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.date = date;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", accountNumber=" + accountNumber +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                '}';
    }
}
