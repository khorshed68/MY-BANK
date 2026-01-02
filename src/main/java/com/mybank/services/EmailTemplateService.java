package com.mybank.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Email Template Service
 * Generates formatted email content for various banking events
 */
public class EmailTemplateService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");
    
    /**
     * Generates login notification email
     */
    public static String loginNotification(String customerName, String maskedAccount, String loginTime, String ipAddress) {
        return String.format(
            "Subject: Successful Login to Your MY BANK Account\n\n" +
            "Dear %s,\n\n" +
            "We detected a successful login to your MY BANK account.\n\n" +
            "Account Number: %s\n" +
            "Login Time: %s\n" +
            "IP Address: %s\n\n" +
            "If this was you, no action is required. If you did not perform this login, " +
            "please contact our customer support immediately and change your password.\n\n" +
            "Security Tips:\n" +
            "- Never share your password with anyone\n" +
            "- Always logout after completing your transactions\n" +
            "- Monitor your account regularly for suspicious activity\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Security Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, maskedAccount, loginTime, ipAddress
        );
    }
    
    /**
     * Generates logout notification email
     */
    public static String logoutNotification(String customerName, String maskedAccount, String logoutTime) {
        return String.format(
            "Subject: Logout from Your MY BANK Account\n\n" +
            "Dear %s,\n\n" +
            "You have successfully logged out from your MY BANK account.\n\n" +
            "Account Number: %s\n" +
            "Logout Time: %s\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, maskedAccount, logoutTime
        );
    }
    
    /**
     * Generates deposit notification email
     */
    public static String depositNotification(String customerName, String maskedAccount, double amount, 
                                             double newBalance, String transactionTime) {
        return String.format(
            "Subject: Deposit Confirmation - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "Your deposit transaction has been successfully processed.\n\n" +
            "Transaction Details:\n" +
            "Account Number: %s\n" +
            "Transaction Type: DEPOSIT\n" +
            "Amount Deposited: %.2f TAKA\n" +
            "New Balance: %.2f TAKA\n" +
            "Transaction Time: %s\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "For any queries, please contact our customer support.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, maskedAccount, amount, newBalance, transactionTime
        );
    }
    
    /**
     * Generates withdrawal notification email
     */
    public static String withdrawalNotification(String customerName, String maskedAccount, double amount, 
                                                double newBalance, String transactionTime) {
        return String.format(
            "Subject: Withdrawal Confirmation - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "Your withdrawal transaction has been successfully processed.\n\n" +
            "Transaction Details:\n" +
            "Account Number: %s\n" +
            "Transaction Type: WITHDRAWAL\n" +
            "Amount Withdrawn: %.2f TAKA\n" +
            "New Balance: %.2f TAKA\n" +
            "Transaction Time: %s\n\n" +
            "If you did not authorize this transaction, please contact our customer support immediately.\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, maskedAccount, amount, newBalance, transactionTime
        );
    }
    
    /**
     * Generates transfer sent notification email
     */
    public static String transferSentNotification(String customerName, String maskedAccount, double amount, 
                                                  String recipientAccount, double newBalance, String transactionTime) {
        return String.format(
            "Subject: Fund Transfer Sent - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "Your fund transfer has been successfully completed.\n\n" +
            "Transaction Details:\n" +
            "From Account: %s\n" +
            "To Account: ****%s\n" +
            "Transaction Type: TRANSFER (SENT)\n" +
            "Amount Transferred: %.2f TAKA\n" +
            "Your New Balance: %.2f TAKA\n" +
            "Transaction Time: %s\n\n" +
            "If you did not authorize this transaction, please contact our customer support immediately.\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, maskedAccount, recipientAccount, amount, newBalance, transactionTime
        );
    }
    
    /**
     * Generates transfer received notification email
     */
    public static String transferReceivedNotification(String customerName, String maskedAccount, double amount, 
                                                      String senderAccount, double newBalance, String transactionTime) {
        return String.format(
            "Subject: Fund Transfer Received - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "You have received a fund transfer.\n\n" +
            "Transaction Details:\n" +
            "To Your Account: %s\n" +
            "From Account: ****%s\n" +
            "Transaction Type: TRANSFER (RECEIVED)\n" +
            "Amount Received: %.2f TAKA\n" +
            "Your New Balance: %.2f TAKA\n" +
            "Transaction Time: %s\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, maskedAccount, senderAccount, amount, newBalance, transactionTime
        );
    }
    
    /**
     * Generates password change notification email
     */
    public static String passwordChangeNotification(String customerName, String maskedAccount, String changeTime) {
        return String.format(
            "Subject: Password Changed - MY BANK Security Alert\n\n" +
            "Dear %s,\n\n" +
            "Your MY BANK account password has been successfully changed.\n\n" +
            "Account Number: %s\n" +
            "Change Time: %s\n\n" +
            "IMPORTANT SECURITY NOTICE:\n" +
            "If you did NOT make this change, your account may be compromised. " +
            "Please contact our customer support IMMEDIATELY at:\n" +
            "- Phone: 1-800-MYBANK-HELP\n" +
            "- Email: security@mybank.com\n\n" +
            "We recommend:\n" +
            "1. Change your password immediately\n" +
            "2. Review recent account activity\n" +
            "3. Enable two-factor authentication\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Security Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, maskedAccount, changeTime
        );
    }
    
    /**
     * Generates suspicious login attempt notification email
     */
    public static String suspiciousLoginNotification(String customerName, String maskedAccount, 
                                                     String attemptTime, String ipAddress, int failedAttempts) {
        return String.format(
            "Subject: SECURITY ALERT - Suspicious Login Attempt - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "⚠️ SECURITY ALERT ⚠️\n\n" +
            "We detected a suspicious login attempt on your MY BANK account.\n\n" +
            "Alert Details:\n" +
            "Account Number: %s\n" +
            "Attempt Time: %s\n" +
            "IP Address: %s\n" +
            "Failed Attempts: %d\n\n" +
            "IMMEDIATE ACTION REQUIRED:\n" +
            "If this was NOT you, please:\n" +
            "1. Contact our security team immediately\n" +
            "2. Change your password\n" +
            "3. Review your account for unauthorized activity\n\n" +
            "Note: Your account will be automatically blocked after 3 failed login attempts.\n\n" +
            "Contact our Security Team:\n" +
            "- Phone: 1-800-MYBANK-HELP\n" +
            "- Email: security@mybank.com\n\n" +
            "Best regards,\n" +
            "MY BANK Security Team\n\n" +
            "---\n" +
            "This is an automated security alert. Please do not reply to this email.",
            customerName, maskedAccount, attemptTime, ipAddress, failedAttempts
        );
    }
    
    /**
     * Generates account blocked notification email
     */
    public static String accountBlockedNotification(String customerName, String maskedAccount, String blockTime, String reason) {
        return String.format(
            "Subject: 🔒 ACCOUNT BLOCKED - Immediate Action Required\n\n" +
            "Dear %s,\n\n" +
            "⚠️ SECURITY ALERT - YOUR ACCOUNT HAS BEEN BLOCKED ⚠️\n\n" +
            "Your MY BANK account has been BLOCKED for your protection.\n\n" +
            "═══════════════════════════════════════════\n" +
            "           BLOCK DETAILS\n" +
            "═══════════════════════════════════════════\n\n" +
            "Account Number: %s\n" +
            "Block Time: %s\n" +
            "Reason: %s\n" +
            "Status: LOCKED\n\n" +
            "═══════════════════════════════════════════\n" +
            "           WHAT THIS MEANS\n" +
            "═══════════════════════════════════════════\n\n" +
            "✓ Your account is temporarily locked\n" +
            "✓ You CANNOT login or perform transactions\n" +
            "✓ Your funds are SAFE and SECURE\n" +
            "✓ This is a security measure to protect you\n\n" +
            "═══════════════════════════════════════════\n" +
            "        HOW TO UNBLOCK YOUR ACCOUNT\n" +
            "═══════════════════════════════════════════\n\n" +
            "You have THREE options to unblock your account:\n\n" +
            "OPTION 1: Call Customer Support\n" +
            "   ⏰ Hours: 24/7 Support Available\n" +
            "   📋 Have Ready: Your ID and account number\n\n" +
            "OPTION 2: Email Support\n" +
            "   📝 Include: Full name, account number, phone number\n" +
            "   📎 Attach: Copy of your ID (for verification)\n" +
            "   ⏱️ Response: Within 24 hours\n\n" +
            "OPTION 3: Visit a Branch\n" +
            "   🏢 Visit: Any MY BANK branch\n" +
            "   🆔 Bring: Valid government-issued ID\n" +
            "   ✅ Instant: Account unblocked immediately\n\n" +
            "═══════════════════════════════════════════\n" +
            "           VERIFICATION PROCESS\n" +
            "═══════════════════════════════════════════\n\n" +
            "Our staff will:\n" +
            "1. Verify your identity with ID\n" +
            "2. Confirm account ownership\n" +
            "3. Ask security questions\n" +
            "4. Reset failed login attempts\n" +
            "5. Unblock your account immediately\n" +
            "6. Help you reset your password (if needed)\n\n" +
            "═══════════════════════════════════════════\n" +
            "           SECURITY TIPS\n" +
            "═══════════════════════════════════════════\n\n" +
            "To prevent future blocks:\n" +
            "• Use a strong, memorable password\n" +
            "• Write it down in a secure place\n" +
            "• Don't share your password with anyone\n" +
            "• Contact us if you forget your password\n" +
            "• Enable email notifications for all activity\n\n" +
            "═══════════════════════════════════════════\n" +
            "           NEED HELP?\n" +
            "═══════════════════════════════════════════\n\n" +
            "We're here to help you 24/7:\n" +
            " Chat: www.mybank.com/support\n" +
            "🏢 Branch Locator: www.mybank.com/branches\n\n" +
            "Your security is our top priority.\n" +
            "We apologize for any inconvenience.\n\n" +
            "Best regards,\n" +
            "MY BANK Security Team\n\n" +
            "---\n" +
            "This is an automated security alert.\n" +
            "Please do not reply to this email.\n" +
            "For assistance, use the contact methods above.",
            customerName, maskedAccount, blockTime, reason
        );
    }
    
    /**
     * Generates account reactivated notification email
     */
    public static String accountReactivatedNotification(String customerName, String maskedAccount, String reactivationTime) {
        return String.format(
            "Subject: Account Reactivated - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "Good news! Your MY BANK account has been successfully reactivated.\n\n" +
            "Account Number: %s\n" +
            "Reactivation Time: %s\n\n" +
            "You can now:\n" +
            "- Access your account\n" +
            "- Perform transactions\n" +
            "- Use all banking services\n\n" +
            "For your security, we recommend:\n" +
            "1. Change your password if you haven't already\n" +
            "2. Review recent account activity\n" +
            "3. Enable email and SMS notifications\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, maskedAccount, reactivationTime
        );
    }
    
    /**
     * Generates account approval notification email
     * Sent when a new account opening request is approved by staff
     */
    public static String accountApprovalNotification(String customerName, String accountNumber, 
                                                     String accountType, double initialBalance,
                                                     String defaultPassword, String approvalTime) {
        return String.format(
            "Subject: 🎉 Account Approved - Welcome to MY BANK!\n\n" +
            "Dear %s,\n\n" +
            "Congratulations! Your account opening request has been APPROVED.\n\n" +
            "═══════════════════════════════════════════\n" +
            "           YOUR ACCOUNT DETAILS\n" +
            "═══════════════════════════════════════════\n\n" +
            "Account Number: %s\n" +
            "Account Type: %s\n" +
            "Initial Balance: %.2f TAKA\n" +
            "Approval Time: %s\n" +
            "Status: ACTIVE\n\n" +
            "═══════════════════════════════════════════\n" +
            "           YOUR LOGIN CREDENTIALS\n" +
            "═══════════════════════════════════════════\n\n" +
            "Login Phone Number: (Use the phone number you provided)\n" +
            "Your Unique Password: %s\n\n" +
            "⚠️ IMPORTANT SECURITY NOTICE ⚠️\n" +
            "• This is YOUR UNIQUE PASSWORD - different from other customers\n" +
            "• Keep this password CONFIDENTIAL and SECURE\n" +
            "• You MUST change this password on your first login\n" +
            "• Never share this password with anyone\n\n" +
            "═══════════════════════════════════════════\n" +
            "           HOW TO GET STARTED\n" +
            "═══════════════════════════════════════════\n\n" +
            "1. Open the MY BANK application\n" +
            "2. Login with:\n" +
            "   • Phone Number: Your registered phone number\n" +
            "   • Password: %s\n" +
            "3. Start banking!\n\n" +
            "═══════════════════════════════════════════\n" +
            "           NOTIFICATION SYSTEM\n" +
            "═══════════════════════════════════════════\n\n" +
            "You will receive EMAIL notifications for:\n" +
            "✓ Every login and logout\n" +
            "✓ All transactions (deposits, withdrawals, transfers)\n" +
            "✓ Password changes\n" +
            "✓ Security alerts\n" +
            "✓ Account updates\n\n" +
            "Note: All notifications will be sent to this email address.\n" +
            "We do not send SMS notifications.\n\n" +
            "═══════════════════════════════════════════\n" +
            "           CUSTOMER SUPPORT\n" +
            "═══════════════════════════════════════════\n\n" +
            "Need help? Contact us:\n" +
            "🏢 Visit any MY BANK branch\n\n" +
            "Thank you for choosing MY BANK!\n" +
            "We're excited to serve you.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.\n" +
            "Keep your password secure and never share it with anyone.",
            customerName, accountNumber, accountType, initialBalance, approvalTime, 
            defaultPassword, defaultPassword
        );
    }
    
    /**
     * Generates cheque book request notification email
     */
    public static String chequeBookRequestNotification(String customerName, String accountNumber, 
                                                       String bookNumber, int leaves, String requestTime) {
        return String.format(
            "Subject: Cheque Book Request Received - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "Your cheque book request has been successfully received and is pending approval.\n\n" +
            "Request Details:\n" +
            "Account Number: %s\n" +
            "Book Number: %s\n" +
            "Number of Leaves: %d\n" +
            "Request Time: %s\n" +
            "Status: PENDING APPROVAL\n\n" +
            "Your request will be reviewed by our staff and you will be notified once a decision is made.\n\n" +
            "Processing Time: Usually 1-2 business days\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, accountNumber, bookNumber, leaves, requestTime
        );
    }
    
    /**
     * Generates cheque book approval notification email
     */
    public static String chequeBookApprovalNotification(String customerName, String accountNumber, 
                                                        String bookNumber, int leaves, 
                                                        String startCheque, String endCheque, 
                                                        String approvalTime) {
        return String.format(
            "Subject: ✅ Cheque Book Approved - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "Good news! Your cheque book request has been APPROVED and issued.\n\n" +
            "═══════════════════════════════════════════\n" +
            "        CHEQUE BOOK DETAILS\n" +
            "═══════════════════════════════════════════\n\n" +
            "Account Number: %s\n" +
            "Book Number: %s\n" +
            "Number of Leaves: %d\n" +
            "Cheque Number Range: %s to %s\n" +
            "Approval Time: %s\n" +
            "Status: ISSUED\n\n" +
            "Your cheque book is now ready for use. You can:\n" +
            "• View all your cheques in the Cheque Management section\n" +
            "• Use cheques for payments\n" +
            "• Track cheque status in real-time\n\n" +
            "Important Reminders:\n" +
            "⚠️ Write cheques carefully and ensure sufficient balance\n" +
            "⚠️ Sign cheques exactly as per your signature on file\n" +
            "⚠️ Keep your cheque book secure and report any loss immediately\n" +
            "⚠️ Bounced cheques may result in penalties and affect your banking profile\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, accountNumber, bookNumber, leaves, startCheque, endCheque, approvalTime
        );
    }
    
    /**
     * Generates cheque book rejection notification email
     */
    public static String chequeBookRejectionNotification(String customerName, String accountNumber, 
                                                         String bookNumber, String reason, 
                                                         String rejectionTime) {
        return String.format(
            "Subject: ❌ Cheque Book Request Declined - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "We regret to inform you that your cheque book request has been declined.\n\n" +
            "Request Details:\n" +
            "Account Number: %s\n" +
            "Book Number: %s\n" +
            "Rejection Time: %s\n\n" +
            "Reason for Rejection:\n" +
            "%s\n\n" +
            "What You Can Do:\n" +
            "• Review the rejection reason and address any issues\n" +
            "• Ensure your account meets all eligibility criteria\n" +
            "• Contact customer support for assistance\n" +
            "• Submit a new request once requirements are met\n\n" +
            "Eligibility Requirements:\n" +
            "✓ Maintain minimum balance requirement\n" +
            "✓ Account must meet minimum age requirement\n" +
            "✓ Annual cheque book limit not exceeded\n" +
            "✓ Account in good standing\n\n" +
            "For assistance, please contact your branch.\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, accountNumber, bookNumber, rejectionTime, reason
        );
    }
    
    /**
     * Generates cheque cleared notification email
     */
    public static String chequeClearedNotification(String customerName, String accountNumber, 
                                                   String chequeNumber, double amount, 
                                                   double newBalance, String clearanceTime) {
        return String.format(
            "Subject: ✅ Cheque Cleared - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "A cheque issued from your account has been successfully cleared.\n\n" +
            "Transaction Details:\n" +
            "Account Number: %s\n" +
            "Cheque Number: %s\n" +
            "Amount Debited: %.2f TAKA\n" +
            "New Balance: %.2f TAKA\n" +
            "Clearance Time: %s\n\n" +
            "The amount has been debited from your account and the transaction is complete.\n\n" +
            "If you did not authorize this cheque or notice any discrepancy, " +
            "please contact our customer support immediately.\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, accountNumber, chequeNumber, amount, newBalance, clearanceTime
        );
    }
    
    /**
     * Generates cheque bounced notification email
     */
    public static String chequeBouncedNotification(String customerName, String accountNumber, 
                                                   String chequeNumber, double amount, 
                                                   String bounceReason, String bounceTime) {
        return String.format(
            "Subject: ⚠️ URGENT: Cheque Bounced - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "IMPORTANT: A cheque issued from your account has been BOUNCED.\n\n" +
            "═══════════════════════════════════════════\n" +
            "        BOUNCED CHEQUE DETAILS\n" +
            "═══════════════════════════════════════════\n\n" +
            "Account Number: %s\n" +
            "Cheque Number: %s\n" +
            "Amount: %.2f TAKA\n" +
            "Bounce Time: %s\n\n" +
            "Reason for Bounce:\n" +
            "%s\n\n" +
            "⚠️ IMPORTANT CONSEQUENCES ⚠️\n" +
            "• Your account reputation may be affected\n" +
            "• Penalties may be applied as per bank policy\n" +
            "• Repeated bounces may affect future cheque book eligibility\n" +
            "• The payee will be notified of the bounce\n\n" +
            "Immediate Actions Required:\n" +
            "1. Ensure sufficient balance in your account\n" +
            "2. Contact the payee to make alternative payment arrangements\n" +
            "3. Review your account activity to prevent future occurrences\n" +
            "4. Contact customer support if you have concerns\n\n" +
            "Common Reasons for Bounced Cheques:\n" +
            "• Insufficient balance in account\n" +
            "• Signature mismatch\n" +
            "• Cheque post-dated or stale-dated\n" +
            "• Account frozen or closed\n\n" +
            "For assistance, please contact your branch.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, accountNumber, chequeNumber, amount, bounceTime, bounceReason
        );
    }
    
    /**
     * Generates cheque deposited notification email (for depositor)
     */
    public static String chequeDepositedNotification(String customerName, String accountNumber, 
                                                     String chequeNumber, double amount, 
                                                     String payerName, String depositTime) {
        return String.format(
            "Subject: Cheque Deposited - MY BANK\n\n" +
            "Dear %s,\n\n" +
            "A cheque has been deposited to your account and is pending clearance.\n\n" +
            "Deposit Details:\n" +
            "Account Number: %s\n" +
            "Cheque Number: %s\n" +
            "Amount: %.2f TAKA\n" +
            "Payer: %s\n" +
            "Deposit Time: %s\n" +
            "Status: PENDING CLEARANCE\n\n" +
            "The cheque is currently being processed. Funds will be credited to your account " +
            "after successful clearance, which typically takes 2-3 business days.\n\n" +
            "You will be notified once the cheque is:\n" +
            "✓ Cleared and funds credited\n" +
            "✗ Bounced due to insufficient funds or other issues\n\n" +
            "Thank you for banking with MY BANK.\n\n" +
            "Best regards,\n" +
            "MY BANK Team\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            customerName, accountNumber, chequeNumber, amount, payerName, depositTime
        );
    }
    
    /**
     * Gets current timestamp in formatted string
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }
}
