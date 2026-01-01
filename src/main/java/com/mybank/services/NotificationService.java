package com.mybank.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.mybank.database.DatabaseHelper;
import com.mybank.models.AccountInfo;

/**
 * Notification Service
 * Centralized service for sending Email notifications
 * Handles notification preferences, logging, and delivery
 * 
 * Note: Customers provide both phone and email during registration,
 * but all notifications are sent via EMAIL ONLY (no SMS)
 */
public class NotificationService {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");
    private DatabaseHelper dbHelper;
    private EmailService emailService;
    private boolean servicesInitialized = false;
    
    public NotificationService() {
        this.dbHelper = new DatabaseHelper();
        initializeServices();
    }
    
    /**
     * Initialize email service with error handling
     */
    private void initializeServices() {
        try {
            this.emailService = new EmailService();
            this.servicesInitialized = true;
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize email service: " + e.getMessage());
            System.err.println("Email notifications will be printed to console only (simulated mode)");
            this.servicesInitialized = false;
        }
    }
    
    /**
     * Send login notification
     */
    public void sendLoginNotification(int accountNumber) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        String ipAddress = "127.0.0.1"; // In production, get actual IP
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.loginNotification(
                account.getOwnerName(), 
                account.getMaskedAccountNumber(), 
                timestamp, 
                ipAddress
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "LOGIN", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", "Login from IP: " + ipAddress);
        }
    }
    
    /**
     * Send logout notification
     */
    public void sendLogoutNotification(int accountNumber) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.logoutNotification(
                account.getOwnerName(), 
                account.getMaskedAccountNumber(), 
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "LOGOUT", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", "User logged out");
        }
    }
    
    /**
     * Send deposit notification
     */
    public void sendDepositNotification(int accountNumber, double amount, double newBalance) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.depositNotification(
                account.getOwnerName(), 
                account.getMaskedAccountNumber(), 
                amount, 
                newBalance, 
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "DEPOSIT", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", String.format("Deposit: %.2f TAKA", amount));
        }
    }
    
    /**
     * Send withdrawal notification
     */
    public void sendWithdrawalNotification(int accountNumber, double amount, double newBalance) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.withdrawalNotification(
                account.getOwnerName(), 
                account.getMaskedAccountNumber(), 
                amount, 
                newBalance, 
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "WITHDRAWAL", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", String.format("Withdrawal: %.2f TAKA", amount));
        }
    }
    
    /**
     * Send transfer notifications (both sender and receiver)
     */
    public void sendTransferNotifications(int fromAccount, int toAccount, double amount, 
                                         double senderNewBalance, double receiverNewBalance) {
        String timestamp = getCurrentTimestamp();
        
        // Notify sender
        AccountInfo sender = getAccountInfo(fromAccount);
        if (sender != null) {
            String toAccountStr = String.valueOf(toAccount);
            String last4 = toAccountStr.length() >= 4 ? toAccountStr.substring(toAccountStr.length() - 4) : toAccountStr;
            
            if (sender.isEmailEnabled() && sender.getEmail() != null && !sender.getEmail().isEmpty()) {
                String emailMessage = EmailTemplateService.transferSentNotification(
                    sender.getOwnerName(), 
                    sender.getMaskedAccountNumber(), 
                    amount, 
                    last4, 
                    senderNewBalance, 
                    timestamp
                );
                sendEmail(sender.getEmail(), emailMessage);
                logNotification(fromAccount, "TRANSFER_SENT", "EMAIL", sender.getEmail(), 
                              emailMessage, "SENT", String.format("Transfer to %d: %.2f TAKA", toAccount, amount));
            }
        }
        
        // Notify receiver
        AccountInfo receiver = getAccountInfo(toAccount);
        if (receiver != null) {
            String fromAccountStr = String.valueOf(fromAccount);
            String last4 = fromAccountStr.length() >= 4 ? fromAccountStr.substring(fromAccountStr.length() - 4) : fromAccountStr;
            
            if (receiver.isEmailEnabled() && receiver.getEmail() != null && !receiver.getEmail().isEmpty()) {
                String emailMessage = EmailTemplateService.transferReceivedNotification(
                    receiver.getOwnerName(), 
                    receiver.getMaskedAccountNumber(), 
                    amount, 
                    last4, 
                    receiverNewBalance, 
                    timestamp
                );
                sendEmail(receiver.getEmail(), emailMessage);
                logNotification(toAccount, "TRANSFER_RECEIVED", "EMAIL", receiver.getEmail(), 
                              emailMessage, "SENT", String.format("Transfer from %d: %.2f TAKA", fromAccount, amount));
            }
        }
    }
    
    /**
     * Send password change notification
     */
    public void sendPasswordChangeNotification(int accountNumber) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.passwordChangeNotification(
                account.getOwnerName(), 
                account.getMaskedAccountNumber(), 
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "PASSWORD_CHANGE", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", "Password changed");
        }
    }
    
    /**
     * Send suspicious login attempt notification
     */
    public void sendSuspiciousLoginNotification(int accountNumber, int failedAttempts) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        String ipAddress = "127.0.0.1"; // In production, get actual IP
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.suspiciousLoginNotification(
                account.getOwnerName(), 
                account.getMaskedAccountNumber(), 
                timestamp, 
                ipAddress, 
                failedAttempts
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "SUSPICIOUS_LOGIN", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", String.format("Failed attempts: %d", failedAttempts));
        }
    }
    
    /**
     * Send account blocked notification
     */
    public void sendAccountBlockedNotification(int accountNumber, String reason) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.accountBlockedNotification(
                account.getOwnerName(), 
                account.getMaskedAccountNumber(), 
                timestamp, 
                reason
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "ACCOUNT_BLOCKED", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", "Reason: " + reason);
        }
    }
    
    /**
     * Send account reactivated notification
     */
    public void sendAccountReactivatedNotification(int accountNumber) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.accountReactivatedNotification(
                account.getOwnerName(), 
                account.getMaskedAccountNumber(), 
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "ACCOUNT_REACTIVATED", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", "Account reactivated");
        }
    }
    
    /**
     * Send account approval notification
     * Sent when staff approves a new account opening request
     */
    public void sendAccountApprovalNotification(int accountNumber, String defaultPassword) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.accountApprovalNotification(
                account.getOwnerName(), 
                String.valueOf(accountNumber), 
                account.getAccountType(),
                account.getBalance(),
                defaultPassword,
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "ACCOUNT_APPROVED", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", "New account approved and created");
        }
    }
    
    /**
     * Retrieves account information including contact details
     */
    private AccountInfo getAccountInfo(int accountNumber) {
        String sql = "SELECT accountNumber, ownerName, balance, email, phoneNumber, smsEnabled, emailEnabled, status, accountType " +
                     "FROM accounts WHERE accountNumber = ?";
        
        try {
            Connection conn = dbHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                AccountInfo account = new AccountInfo();
                account.setAccountNumber(rs.getInt("accountNumber"));
                account.setOwnerName(rs.getString("ownerName"));
                account.setBalance(rs.getDouble("balance"));
                account.setEmail(rs.getString("email"));
                account.setPhoneNumber(rs.getString("phoneNumber"));
                account.setSmsEnabled(rs.getInt("smsEnabled") == 1);
                account.setEmailEnabled(rs.getInt("emailEnabled") == 1);
                account.setStatus(rs.getString("status"));
                account.setAccountType(rs.getString("accountType"));
                return account;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving account info: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Send cheque book request notification
     */
    public void sendChequeBookRequestNotification(int accountNumber, String bookNumber, int leaves) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.chequeBookRequestNotification(
                account.getOwnerName(), 
                String.valueOf(accountNumber), 
                bookNumber, 
                leaves, 
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "CHEQUE_BOOK_REQUEST", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", String.format("Cheque Book Request: %s (%d leaves)", bookNumber, leaves));
        }
    }
    
    /**
     * Send cheque book approval notification
     */
    public void sendChequeBookApprovalNotification(int accountNumber, String bookNumber, int leaves,
                                                   String startCheque, String endCheque) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.chequeBookApprovalNotification(
                account.getOwnerName(), 
                String.valueOf(accountNumber), 
                bookNumber, 
                leaves,
                startCheque,
                endCheque,
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "CHEQUE_BOOK_APPROVED", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", String.format("Cheque Book Approved: %s", bookNumber));
        }
    }
    
    /**
     * Send cheque book rejection notification
     */
    public void sendChequeBookRejectionNotification(int accountNumber, String bookNumber, String reason) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.chequeBookRejectionNotification(
                account.getOwnerName(), 
                String.valueOf(accountNumber), 
                bookNumber, 
                reason, 
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "CHEQUE_BOOK_REJECTED", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", String.format("Cheque Book Rejected: %s - %s", bookNumber, reason));
        }
    }
    
    /**
     * Send cheque cleared notification
     */
    public void sendChequeClearedNotification(int accountNumber, String chequeNumber, double amount, double newBalance) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.chequeClearedNotification(
                account.getOwnerName(), 
                String.valueOf(accountNumber), 
                chequeNumber, 
                amount,
                newBalance,
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "CHEQUE_CLEARED", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", String.format("Cheque Cleared: %s, Amount: %.2f TAKA", chequeNumber, amount));
        }
    }
    
    /**
     * Send cheque bounced notification
     */
    public void sendChequeBouncedNotification(int accountNumber, String chequeNumber, double amount, String bounceReason) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.chequeBouncedNotification(
                account.getOwnerName(), 
                String.valueOf(accountNumber), 
                chequeNumber, 
                amount,
                bounceReason,
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "CHEQUE_BOUNCED", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", String.format("Cheque Bounced: %s - %s", chequeNumber, bounceReason));
        }
    }
    
    /**
     * Send cheque deposited notification (for depositor)
     */
    public void sendChequeDepositedNotification(int accountNumber, String chequeNumber, double amount, String payerName) {
        AccountInfo account = getAccountInfo(accountNumber);
        if (account == null) return;
        
        String timestamp = getCurrentTimestamp();
        
        // Send Email
        if (account.isEmailEnabled() && account.getEmail() != null && !account.getEmail().isEmpty()) {
            String emailMessage = EmailTemplateService.chequeDepositedNotification(
                account.getOwnerName(), 
                String.valueOf(accountNumber), 
                chequeNumber, 
                amount,
                payerName,
                timestamp
            );
            sendEmail(account.getEmail(), emailMessage);
            logNotification(accountNumber, "CHEQUE_DEPOSITED", "EMAIL", account.getEmail(), 
                          emailMessage, "SENT", String.format("Cheque Deposited: %s from %s", chequeNumber, payerName));
        }
    }
    
    /**
     * Sends an email using EmailService
     */
    private void sendEmail(String recipient, String message) {
        if (!servicesInitialized || emailService == null) {
            // Fallback to simulated email
            printSimulatedEmail(recipient, message);
            return;
        }
        
        try {
            // Extract subject from message (first line or default subject)
            String subject = "My Bank - Account Notification";
            
            // Try to extract subject from message if it's formatted
            if (message.contains("Subject:")) {
                int subjectStart = message.indexOf("Subject:") + 8;
                int subjectEnd = message.indexOf("\n", subjectStart);
                if (subjectEnd > subjectStart) {
                    subject = message.substring(subjectStart, subjectEnd).trim();
                }
            }
            
            // Send actual email
            boolean success = emailService.sendEmail(recipient, subject, message);
            
            if (!success) {
                System.err.println("Warning: Failed to send email to " + recipient);
            }
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            printSimulatedEmail(recipient, message);
        }
    }
    
    /**
     * Print simulated email to console (fallback when services unavailable)
     */
    private void printSimulatedEmail(String recipient, String message) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📧 SIMULATED EMAIL");
        System.out.println("TO: " + recipient);
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println(message);
        System.out.println("═══════════════════════════════════════════════════════\n");
    }
    
    /**
     * Logs notification to database for auditing
     */
    private void logNotification(int accountNumber, String notificationType, String channel, 
                                 String recipient, String message, String status, String eventDetails) {
        String sql = "INSERT INTO notifications_log (accountNumber, notificationType, channel, recipient, " +
                     "message, status, eventDetails) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
            Connection conn = dbHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, accountNumber);
            pstmt.setString(2, notificationType);
            pstmt.setString(3, channel);
            pstmt.setString(4, recipient);
            pstmt.setString(5, message.length() > 500 ? message.substring(0, 500) + "..." : message);
            pstmt.setString(6, status);
            pstmt.setString(7, eventDetails);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging notification: " + e.getMessage());
        }
    }
    
    /**
     * Gets current timestamp
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
}
