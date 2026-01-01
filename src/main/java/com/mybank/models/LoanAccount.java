package com.mybank.models;

/**
 * Loan Account Model
 * Represents loan-specific information for loan accounts
 */
public class LoanAccount {
    
    private int loanId;
    private int accountNumber;
    private double loanAmount;
    private double outstandingBalance;
    private double interestRate;
    private double installmentAmount;
    private String nextDueDate;
    private String repaymentStatus;    // ACTIVE, PAID, OVERDUE, DEFAULTED
    private int loanTerm;              // in months
    private String disbursementDate;
    private String lastPaymentDate;
    
    // Constructors
    public LoanAccount() {
    }
    
    public LoanAccount(int loanId, int accountNumber, double loanAmount, double outstandingBalance,
                      double interestRate, double installmentAmount, String nextDueDate,
                      String repaymentStatus, int loanTerm, String disbursementDate, String lastPaymentDate) {
        this.loanId = loanId;
        this.accountNumber = accountNumber;
        this.loanAmount = loanAmount;
        this.outstandingBalance = outstandingBalance;
        this.interestRate = interestRate;
        this.installmentAmount = installmentAmount;
        this.nextDueDate = nextDueDate;
        this.repaymentStatus = repaymentStatus;
        this.loanTerm = loanTerm;
        this.disbursementDate = disbursementDate;
        this.lastPaymentDate = lastPaymentDate;
    }
    
    /**
     * Returns masked account number for security
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
     * Calculates percentage of loan repaid
     * @return Percentage repaid (0-100)
     */
    public double getRepaymentPercentage() {
        if (loanAmount == 0) return 0;
        double paidAmount = loanAmount - outstandingBalance;
        return (paidAmount / loanAmount) * 100.0;
    }
    
    /**
     * Calculates total amount paid so far
     * @return Amount paid
     */
    public double getAmountPaid() {
        return loanAmount - outstandingBalance;
    }
    
    /**
     * Calculates total interest to be paid
     * @return Total interest
     */
    public double getTotalInterest() {
        double totalRepayment = installmentAmount * loanTerm;
        return totalRepayment - loanAmount;
    }
    
    /**
     * Returns repayment status display string
     * @return Formatted repayment status
     */
    public String getRepaymentStatusDisplay() {
        switch (repaymentStatus) {
            case "ACTIVE":
                return "Active - On Track";
            case "PAID":
                return "Fully Paid";
            case "OVERDUE":
                return "Overdue";
            case "DEFAULTED":
                return "Defaulted";
            default:
                return repaymentStatus;
        }
    }
    
    /**
     * Checks if loan is currently active
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return "ACTIVE".equals(repaymentStatus);
    }
    
    /**
     * Checks if loan is overdue
     * @return true if overdue, false otherwise
     */
    public boolean isOverdue() {
        return "OVERDUE".equals(repaymentStatus);
    }
    
    /**
     * Checks if loan is fully paid
     * @return true if paid, false otherwise
     */
    public boolean isPaid() {
        return "PAID".equals(repaymentStatus) || outstandingBalance <= 0;
    }
    
    /**
     * Returns formatted loan amount
     * @return Formatted string
     */
    public String getFormattedLoanAmount() {
        return String.format("%.2f TAKA", loanAmount);
    }
    
    /**
     * Returns formatted outstanding balance
     * @return Formatted string
     */
    public String getFormattedOutstandingBalance() {
        return String.format("%.2f TAKA", outstandingBalance);
    }
    
    /**
     * Returns formatted installment amount
     * @return Formatted string
     */
    public String getFormattedInstallmentAmount() {
        return String.format("%.2f TAKA", installmentAmount);
    }
    
    /**
     * Returns formatted interest rate
     * @return Formatted string (e.g., "12.5%")
     */
    public String getFormattedInterestRate() {
        return String.format("%.2f%%", interestRate);
    }
    
    // Getters and Setters
    public int getLoanId() {
        return loanId;
    }
    
    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }
    
    public int getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public double getLoanAmount() {
        return loanAmount;
    }
    
    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }
    
    public double getOutstandingBalance() {
        return outstandingBalance;
    }
    
    public void setOutstandingBalance(double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }
    
    public double getInterestRate() {
        return interestRate;
    }
    
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
    
    public double getInstallmentAmount() {
        return installmentAmount;
    }
    
    public void setInstallmentAmount(double installmentAmount) {
        this.installmentAmount = installmentAmount;
    }
    
    public String getNextDueDate() {
        return nextDueDate != null ? nextDueDate : "N/A";
    }
    
    public void setNextDueDate(String nextDueDate) {
        this.nextDueDate = nextDueDate;
    }
    
    public String getRepaymentStatus() {
        return repaymentStatus;
    }
    
    public void setRepaymentStatus(String repaymentStatus) {
        this.repaymentStatus = repaymentStatus;
    }
    
    public int getLoanTerm() {
        return loanTerm;
    }
    
    public void setLoanTerm(int loanTerm) {
        this.loanTerm = loanTerm;
    }
    
    public String getDisbursementDate() {
        return disbursementDate != null ? disbursementDate : "N/A";
    }
    
    public void setDisbursementDate(String disbursementDate) {
        this.disbursementDate = disbursementDate;
    }
    
    public String getLastPaymentDate() {
        return lastPaymentDate != null ? lastPaymentDate : "No payments yet";
    }
    
    public void setLastPaymentDate(String lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }
    
    @Override
    public String toString() {
        return "LoanAccount{" +
                "loanId=" + loanId +
                ", accountNumber=" + getMaskedAccountNumber() +
                ", loanAmount=" + loanAmount +
                ", outstandingBalance=" + outstandingBalance +
                ", repaymentStatus='" + repaymentStatus + '\'' +
                '}';
    }
}
