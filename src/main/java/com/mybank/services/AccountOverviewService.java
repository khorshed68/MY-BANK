package com.mybank.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mybank.database.DatabaseHelper;
import com.mybank.models.AccountOverview;
import com.mybank.models.LoanAccount;

/**
 * Account Overview Service
 * Provides business logic for retrieving comprehensive account information
 * Ensures secure access - users can only view their own accounts
 */
public class AccountOverviewService {
    
    private DatabaseHelper dbHelper;
    
    public AccountOverviewService() {
        this.dbHelper = new DatabaseHelper();
    }
    
    /**
     * Retrieves complete account overview for a specific account
     * Includes security check to ensure user can only access their own account
     * 
     * @param accountNumber The account number to retrieve
     * @param loggedInAccountNumber The currently logged-in user's account number
     * @return AccountOverview object or null if not found or unauthorized
     */
    public AccountOverview getAccountOverview(int accountNumber, int loggedInAccountNumber) {
        // Security check: users can only view their own account
        if (accountNumber != loggedInAccountNumber) {
            System.err.println("Security violation: User " + loggedInAccountNumber + 
                             " attempted to access account " + accountNumber);
            return null;
        }
        
        String sql = "SELECT accountNumber, ownerName, accountType, balance, availableBalance, " +
                     "status, lastTransactionDate, createdDate, email, phoneNumber " +
                     "FROM accounts WHERE accountNumber = ?";
        
        try {
            Connection conn = dbHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                AccountOverview account = new AccountOverview();
                account.setAccountNumber(rs.getInt("accountNumber"));
                account.setOwnerName(rs.getString("ownerName"));
                account.setAccountType(rs.getString("accountType") != null ? 
                                      rs.getString("accountType") : "SAVINGS");
                account.setBalance(rs.getDouble("balance"));
                account.setAvailableBalance(rs.getDouble("availableBalance"));
                account.setStatus(rs.getString("status"));
                account.setLastTransactionDate(rs.getString("lastTransactionDate"));
                account.setCreatedDate(rs.getString("createdDate"));
                account.setEmail(rs.getString("email"));
                account.setPhoneNumber(rs.getString("phoneNumber"));
                
                // Update available balance if not set (for backward compatibility)
                if (account.getAvailableBalance() == 0 && account.getBalance() > 0) {
                    updateAvailableBalance(accountNumber, account.getBalance());
                    account.setAvailableBalance(account.getBalance());
                }
                
                return account;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving account overview: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Retrieves all accounts owned by a specific user
     * (In current implementation, each user has one account, but this supports future expansion)
     * 
     * @param ownerAccountNumber The logged-in user's account number
     * @return List of AccountOverview objects
     */
    public List<AccountOverview> getAllUserAccounts(int ownerAccountNumber) {
        List<AccountOverview> accounts = new ArrayList<>();
        
        // For now, return only the user's own account
        // In future, this could support users with multiple accounts
        AccountOverview account = getAccountOverview(ownerAccountNumber, ownerAccountNumber);
        if (account != null) {
            accounts.add(account);
        }
        
        return accounts;
    }
    
    /**
     * Retrieves loan information for a specific account
     * Only returns data if the account is a loan account
     * 
     * @param accountNumber The account number
     * @param loggedInAccountNumber The currently logged-in user's account number
     * @return LoanAccount object or null if not found, unauthorized, or not a loan account
     */
    public LoanAccount getLoanAccountInfo(int accountNumber, int loggedInAccountNumber) {
        // Security check
        if (accountNumber != loggedInAccountNumber) {
            System.err.println("Security violation: User " + loggedInAccountNumber + 
                             " attempted to access loan info for account " + accountNumber);
            return null;
        }
        
        String sql = "SELECT loanId, accountNumber, loanAmount, outstandingBalance, interestRate, " +
                     "installmentAmount, nextDueDate, repaymentStatus, loanTerm, " +
                     "disbursementDate, lastPaymentDate " +
                     "FROM loans WHERE accountNumber = ? ORDER BY loanId DESC LIMIT 1";
        
        try {
            Connection conn = dbHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                LoanAccount loan = new LoanAccount();
                loan.setLoanId(rs.getInt("loanId"));
                loan.setAccountNumber(rs.getInt("accountNumber"));
                loan.setLoanAmount(rs.getDouble("loanAmount"));
                loan.setOutstandingBalance(rs.getDouble("outstandingBalance"));
                loan.setInterestRate(rs.getDouble("interestRate"));
                loan.setInstallmentAmount(rs.getDouble("installmentAmount"));
                loan.setNextDueDate(rs.getString("nextDueDate"));
                loan.setRepaymentStatus(rs.getString("repaymentStatus"));
                loan.setLoanTerm(rs.getInt("loanTerm"));
                loan.setDisbursementDate(rs.getString("disbursementDate"));
                loan.setLastPaymentDate(rs.getString("lastPaymentDate"));
                
                return loan;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving loan account info: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Gets summary statistics for an account
     * 
     * @param accountNumber The account number
     * @param loggedInAccountNumber The currently logged-in user's account number
     * @return Array of statistics: [totalTransactions, totalDeposits, totalWithdrawals, totalTransfers]
     */
    public double[] getAccountStatistics(int accountNumber, int loggedInAccountNumber) {
        // Security check
        if (accountNumber != loggedInAccountNumber) {
            return new double[]{0, 0, 0, 0};
        }
        
        double[] stats = new double[4]; // [count, deposits, withdrawals, transfers]
        
        String sql = "SELECT COUNT(*) as count, " +
                     "SUM(CASE WHEN type LIKE '%Deposit%' THEN amount ELSE 0 END) as deposits, " +
                     "SUM(CASE WHEN type LIKE '%Withdraw%' THEN amount ELSE 0 END) as withdrawals, " +
                     "SUM(CASE WHEN type LIKE '%Transfer%' THEN amount ELSE 0 END) as transfers " +
                     "FROM transactions WHERE accountNumber = ?";
        
        try {
            Connection conn = dbHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                stats[0] = rs.getDouble("count");
                stats[1] = rs.getDouble("deposits");
                stats[2] = rs.getDouble("withdrawals");
                stats[3] = rs.getDouble("transfers");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving account statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Updates available balance for an account
     * Available balance = balance - any holds/locks (currently same as balance)
     * 
     * @param accountNumber The account number
     * @param availableBalance The available balance amount
     */
    private void updateAvailableBalance(int accountNumber, double availableBalance) {
        String sql = "UPDATE accounts SET availableBalance = ? WHERE accountNumber = ?";
        
        try {
            Connection conn = dbHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, availableBalance);
            pstmt.setInt(2, accountNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating available balance: " + e.getMessage());
        }
    }
    
    /**
     * Updates last transaction date for an account
     * Should be called after every transaction
     * 
     * @param accountNumber The account number
     */
    public void updateLastTransactionDate(int accountNumber) {
        String sql = "UPDATE accounts SET lastTransactionDate = CURRENT_TIMESTAMP WHERE accountNumber = ?";
        
        try {
            Connection conn = dbHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, accountNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last transaction date: " + e.getMessage());
        }
    }
    
    /**
     * Checks if an account is a loan account
     * 
     * @param accountNumber The account number
     * @return true if loan account, false otherwise
     */
    public boolean isLoanAccount(int accountNumber) {
        AccountOverview account = getAccountOverview(accountNumber, accountNumber);
        return account != null && "LOAN".equals(account.getAccountType());
    }
    
    /**
     * Gets account health status based on balance and status
     * 
     * @param account The account overview object
     * @return Health status: "HEALTHY", "WARNING", "CRITICAL"
     */
    public String getAccountHealth(AccountOverview account) {
        if (account == null) return "UNKNOWN";
        
        if (account.isBlocked() || account.isClosed()) {
            return "CRITICAL";
        }
        
        if (account.getBalance() < 500) {
            return "WARNING";
        }
        
        return "HEALTHY";
    }
}
