package com.mybank.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SMS Template Service
 * Generates concise SMS content for various banking events
 */
public class SMSTemplateService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    
    /**
     * Generates login notification SMS (160 characters max)
     */
    public static String loginNotification(String customerName, String maskedAccount, String loginTime) {
        return String.format(
            "MY BANK Alert: Login detected on account %s at %s. If not you, call 1-800-MYBANK immediately.",
            maskedAccount, loginTime
        );
    }
    
    /**
     * Generates logout notification SMS
     */
    public static String logoutNotification(String customerName, String maskedAccount, String logoutTime) {
        return String.format(
            "MY BANK: Successfully logged out from account %s at %s. Thank you!",
            maskedAccount, logoutTime
        );
    }
    
    /**
     * Generates deposit notification SMS
     */
    public static String depositNotification(String customerName, String maskedAccount, double amount, 
                                             double newBalance, String transactionTime) {
        return String.format(
            "MY BANK: Deposit of %.2f TAKA to account %s successful. New balance: %.2f TAKA. Time: %s",
            amount, maskedAccount, newBalance, transactionTime
        );
    }
    
    /**
     * Generates withdrawal notification SMS
     */
    public static String withdrawalNotification(String customerName, String maskedAccount, double amount, 
                                                double newBalance, String transactionTime) {
        return String.format(
            "MY BANK: Withdrawal of %.2f TAKA from account %s successful. New balance: %.2f TAKA. Time: %s",
            amount, maskedAccount, newBalance, transactionTime
        );
    }
    
    /**
     * Generates transfer sent notification SMS
     */
    public static String transferSentNotification(String customerName, String maskedAccount, double amount, 
                                                  String recipientAccount, double newBalance, String transactionTime) {
        return String.format(
            "MY BANK: Transfer of %.2f TAKA sent from %s to ****%s. New balance: %.2f TAKA. Time: %s",
            amount, maskedAccount, recipientAccount, newBalance, transactionTime
        );
    }
    
    /**
     * Generates transfer received notification SMS
     */
    public static String transferReceivedNotification(String customerName, String maskedAccount, double amount, 
                                                      String senderAccount, double newBalance, String transactionTime) {
        return String.format(
            "MY BANK: Transfer of %.2f TAKA received in account %s from ****%s. New balance: %.2f TAKA. Time: %s",
            amount, maskedAccount, senderAccount, newBalance, transactionTime
        );
    }
    
    /**
     * Generates password change notification SMS
     */
    public static String passwordChangeNotification(String customerName, String maskedAccount, String changeTime) {
        return String.format(
            "MY BANK SECURITY: Password changed for account %s at %s. If not you, call 1-800-MYBANK NOW!",
            maskedAccount, changeTime
        );
    }
    
    /**
     * Generates suspicious login attempt notification SMS
     */
    public static String suspiciousLoginNotification(String customerName, String maskedAccount, 
                                                     String attemptTime, int failedAttempts) {
        return String.format(
            "MY BANK ALERT: Failed login attempt %d on account %s at %s. Account will lock after 3 attempts. Call 1-800-MYBANK.",
            failedAttempts, maskedAccount, attemptTime
        );
    }
    
    /**
     * Generates account blocked notification SMS
     */
    public static String accountBlockedNotification(String customerName, String maskedAccount, String blockTime, String reason) {
        return String.format(
            "MY BANK URGENT: Account %s BLOCKED at %s. Reason: %s. Contact support: 1-800-MYBANK immediately.",
            maskedAccount, blockTime, reason
        );
    }
    
    /**
     * Generates account reactivated notification SMS
     */
    public static String accountReactivatedNotification(String customerName, String maskedAccount, String reactivationTime) {
        return String.format(
            "MY BANK: Account %s reactivated at %s. You can now access all services. Welcome back!",
            maskedAccount, reactivationTime
        );
    }
    
    /**
     * Generates balance update notification SMS
     */
    public static String balanceUpdateNotification(String customerName, String maskedAccount, 
                                                   double oldBalance, double newBalance, String updateTime) {
        return String.format(
            "MY BANK: Balance update for account %s. Previous: %.2f TAKA, Current: %.2f TAKA. Time: %s",
            maskedAccount, oldBalance, newBalance, updateTime
        );
    }
    
    /**
     * Gets current timestamp in formatted string
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }
}
