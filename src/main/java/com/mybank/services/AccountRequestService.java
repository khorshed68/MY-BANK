package com.mybank.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mybank.database.DatabaseHelper;
import com.mybank.models.AccountRequest;

/**
 * Service class for managing account opening requests.
 * Handles creation, approval, rejection, and retrieval of account requests.
 */
public class AccountRequestService {
    
    /**
     * Create a new account opening request
     */
    public static boolean createAccountRequest(AccountRequest request) {
        String query = "INSERT INTO account_requests " +
                      "(customerName, email, phoneNumber, address, identityType, identityNumber, " +
                      "accountType, initialDeposit, requestStatus, profilePicturePath) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, request.getCustomerName());
            pstmt.setString(2, request.getEmail());
            pstmt.setString(3, request.getPhoneNumber());
            pstmt.setString(4, request.getAddress());
            pstmt.setString(5, request.getIdentityType());
            pstmt.setString(6, request.getIdentityNumber());
            pstmt.setString(7, request.getAccountType());
            pstmt.setDouble(8, request.getInitialDeposit());
            pstmt.setString(9, AccountRequest.STATUS_PENDING);
            pstmt.setString(10, request.getProfilePicturePath());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Account request created for: " + request.getCustomerName());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating account request: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get all pending account requests
     */
    public static List<AccountRequest> getPendingRequests() {
        List<AccountRequest> requests = new ArrayList<>();
        
        String query = "SELECT * FROM account_requests WHERE requestStatus = ? " +
                      "ORDER BY requestDate ASC";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, AccountRequest.STATUS_PENDING);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving pending requests: " + e.getMessage());
            e.printStackTrace();
        }
        
        return requests;
    }
    
    /**
     * Get all account requests (all statuses)
     */
    public static List<AccountRequest> getAllRequests(int limit) {
        List<AccountRequest> requests = new ArrayList<>();
        
        String query = "SELECT * FROM account_requests " +
                      "ORDER BY requestDate DESC LIMIT ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all requests: " + e.getMessage());
            e.printStackTrace();
        }
        
        return requests;
    }
    
    /**
     * Get request by ID
     */
    public static AccountRequest getRequestById(int requestId) {
        String query = "SELECT * FROM account_requests WHERE requestId = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, requestId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRequest(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving request: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Approve account request and create account with unique random password
     */
    public static boolean approveRequest(int requestId, int staffId) {
        // Generate unique random password for this customer
        String uniquePassword = generateUniquePassword();
        return approveRequest(requestId, staffId, uniquePassword);
    }
    
    /**
     * Generate a unique random password for new accounts
     * Format: Capital letter + lowercase letters + digits + special char
     * Example: Bank@1a2b3c
     */
    private static String generateUniquePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String special = "!@#$%";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        // Start with "Bank" for consistency
        password.append("Bank");
        
        // Add a special character
        password.append(special.charAt(random.nextInt(special.length())));
        
        // Add 6 random characters (mix of letters and numbers)
        for (int i = 0; i < 6; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Approve account request and create account with specified password
     */
    private static boolean approveRequest(int requestId, int staffId, String initialPassword) {
        AccountRequest request = getRequestById(requestId);
        if (request == null) {
            System.err.println("Request not found: " + requestId);
            return false;
        }
        
        if (!request.isPending()) {
            System.err.println("Request already processed: " + requestId);
            return false;
        }
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Create the customer account
                int accountNumber = DatabaseHelper.createAccountWithConnection(
                    conn,
                    request.getCustomerName(),
                    initialPassword,
                    request.getInitialDeposit(),
                    request.getEmail(),
                    request.getPhoneNumber(),
                    true, // SMS enabled
                    true, // Email enabled
                    request.getAccountType().toUpperCase(),
                    request.getProfilePicturePath() // Pass profile picture path
                );
                
                // Update request status
                String updateQuery = "UPDATE account_requests SET " +
                                   "requestStatus = ?, processedBy = ?, processedDate = ?, " +
                                   "accountNumber = ? WHERE requestId = ?";
                
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    pstmt.setString(1, AccountRequest.STATUS_APPROVED);
                    pstmt.setInt(2, staffId);
                    pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.setInt(4, accountNumber);
                    pstmt.setInt(5, requestId);
                    
                    pstmt.executeUpdate();
                }
                
                conn.commit();
                
                // Log activity after successful commit (outside transaction)
                try {
                    StaffService.logActivity(staffId, "APPROVE_REQUEST", accountNumber,
                        "Approved account request #" + requestId + " for " + request.getCustomerName());
                } catch (Exception logError) {
                    System.err.println("Warning: Failed to log activity: " + logError.getMessage());
                }
                
                // Send account approval notification email
                try {
                    NotificationService notificationService = new NotificationService();
                    notificationService.sendAccountApprovalNotification(accountNumber, initialPassword);
                    System.out.println("Account approval notification sent to: " + request.getEmail());
                } catch (Exception notifError) {
                    System.err.println("Warning: Failed to send approval notification: " + notifError.getMessage());
                    // Don't fail the approval if notification fails
                }
                
                System.out.println("Account request approved. Account number: " + accountNumber);
                return true;
                
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
                System.err.println("Error during approval process: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during approval: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Reject account request
     */
    public static boolean rejectRequest(int requestId, int staffId, String remarks) {
        AccountRequest request = getRequestById(requestId);
        if (request == null) {
            System.err.println("Request not found: " + requestId);
            return false;
        }
        
        if (!request.isPending()) {
            System.err.println("Request already processed: " + requestId);
            return false;
        }
        
        String query = "UPDATE account_requests SET requestStatus = ?, processedBy = ?, " +
                      "processedDate = ?, remarks = ? WHERE requestId = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, AccountRequest.STATUS_REJECTED);
            pstmt.setInt(2, staffId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(4, remarks);
            pstmt.setInt(5, requestId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                StaffService.logActivity(staffId, "REJECT_REQUEST", null,
                    "Rejected account request #" + requestId + " - Reason: " + remarks);
                
                System.out.println("Account request rejected: " + requestId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error rejecting request: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get last created account number from database
     */
    private static int getLastAccountNumber(Connection conn) throws SQLException {
        String query = "SELECT MAX(accountNumber) FROM accounts";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Map ResultSet to AccountRequest object
     */
    private static AccountRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        AccountRequest request = new AccountRequest();
        request.setRequestId(rs.getInt("requestId"));
        request.setCustomerName(rs.getString("customerName"));
        request.setEmail(rs.getString("email"));
        request.setPhoneNumber(rs.getString("phoneNumber"));
        request.setAddress(rs.getString("address"));
        request.setIdentityType(rs.getString("identityType"));
        request.setIdentityNumber(rs.getString("identityNumber"));
        request.setAccountType(rs.getString("accountType"));
        request.setInitialDeposit(rs.getDouble("initialDeposit"));
        request.setRequestStatus(rs.getString("requestStatus"));
        request.setProfilePicturePath(rs.getString("profilePicturePath"));
        
        Timestamp requestTs = rs.getTimestamp("requestDate");
        if (requestTs != null) {
            request.setRequestDate(requestTs.toLocalDateTime());
        }
        
        int processedBy = rs.getInt("processedBy");
        if (!rs.wasNull()) {
            request.setProcessedBy(processedBy);
        }
        
        Timestamp processedTs = rs.getTimestamp("processedDate");
        if (processedTs != null) {
            request.setProcessedDate(processedTs.toLocalDateTime());
        }
        
        request.setRemarks(rs.getString("remarks"));
        
        int accountNumber = rs.getInt("accountNumber");
        if (!rs.wasNull()) {
            request.setAccountNumber(accountNumber);
        }
        
        return request;
    }
}
