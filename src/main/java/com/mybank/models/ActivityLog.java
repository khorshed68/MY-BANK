package com.mybank.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing staff activity log entries.
 * Used for audit trail and compliance tracking of all staff actions.
 */
public class ActivityLog {
    private int logId;
    private int staffId;
    private String staffName;
    private String action;
    private Integer targetAccount;
    private String details;
    private LocalDateTime timestamp;
    private String ipAddress;
    
    // Action Types
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_CREATE_ACCOUNT = "CREATE_ACCOUNT";
    public static final String ACTION_UPDATE_ACCOUNT = "UPDATE_ACCOUNT";
    public static final String ACTION_APPROVE_REQUEST = "APPROVE_REQUEST";
    public static final String ACTION_REJECT_REQUEST = "REJECT_REQUEST";
    public static final String ACTION_VIEW_CUSTOMER = "VIEW_CUSTOMER";
    public static final String ACTION_GENERATE_REPORT = "GENERATE_REPORT";
    public static final String ACTION_DEPOSIT = "DEPOSIT";
    public static final String ACTION_WITHDRAWAL = "WITHDRAWAL";
    public static final String ACTION_TRANSFER = "TRANSFER";
    public static final String ACTION_SUSPEND_ACCOUNT = "SUSPEND_ACCOUNT";
    public static final String ACTION_ACTIVATE_ACCOUNT = "ACTIVATE_ACCOUNT";
    
    // Constructors
    public ActivityLog() {}
    
    public ActivityLog(int staffId, String action, String details) {
        this.staffId = staffId;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    public ActivityLog(int staffId, String action, Integer targetAccount, String details) {
        this.staffId = staffId;
        this.action = action;
        this.targetAccount = targetAccount;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getLogId() {
        return logId;
    }
    
    public void setLogId(int logId) {
        this.logId = logId;
    }
    
    public int getStaffId() {
        return staffId;
    }
    
    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }
    
    public String getStaffName() {
        return staffName;
    }
    
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Integer getTargetAccount() {
        return targetAccount;
    }
    
    public String getTargetAccountString() {
        return targetAccount != null ? targetAccount.toString() : "";
    }
    
    public void setTargetAccount(Integer targetAccount) {
        this.targetAccount = targetAccount;
    }
    
    public void setTargetAccount(String targetAccount) {
        if (targetAccount != null && !targetAccount.isEmpty()) {
            try {
                this.targetAccount = Integer.parseInt(targetAccount);
            } catch (NumberFormatException e) {
                // Keep null if parsing fails
            }
        }
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setTimestamp(String timestampStr) {
        // Handle string timestamp from database
        if (timestampStr != null && !timestampStr.isEmpty()) {
            try {
                this.timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"));
            } catch (Exception e) {
                // Keep null if parsing fails
            }
        }
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    // Utility Methods
    
    /**
     * Get formatted timestamp
     */
    public String getFormattedTimestamp() {
        if (timestamp == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    /**
     * Get action display name
     */
    public String getActionDisplay() {
        switch (action) {
            case ACTION_LOGIN:
                return "Staff Login";
            case ACTION_LOGOUT:
                return "Staff Logout";
            case ACTION_CREATE_ACCOUNT:
                return "Account Created";
            case ACTION_UPDATE_ACCOUNT:
                return "Account Updated";
            case ACTION_APPROVE_REQUEST:
                return "Request Approved";
            case ACTION_REJECT_REQUEST:
                return "Request Rejected";
            case ACTION_VIEW_CUSTOMER:
                return "Customer Viewed";
            case ACTION_GENERATE_REPORT:
                return "Report Generated";
            case ACTION_DEPOSIT:
                return "Deposit Transaction";
            case ACTION_WITHDRAWAL:
                return "Withdrawal Transaction";
            case ACTION_TRANSFER:
                return "Transfer Transaction";
            case ACTION_SUSPEND_ACCOUNT:
                return "Account Suspended";
            case ACTION_ACTIVATE_ACCOUNT:
                return "Account Activated";
            default:
                return action;
        }
    }
    
    /**
     * Get target account display (masked or N/A)
     */
    public String getTargetAccountDisplay() {
        if (targetAccount == null) return "N/A";
        String accountStr = String.valueOf(targetAccount);
        if (accountStr.length() <= 4) return accountStr;
        return "****" + accountStr.substring(accountStr.length() - 4);
    }
    
    /**
     * Check if action is security-sensitive
     */
    public boolean isSecuritySensitive() {
        return action.equals(ACTION_LOGIN) || 
               action.equals(ACTION_CREATE_ACCOUNT) ||
               action.equals(ACTION_APPROVE_REQUEST) ||
               action.equals(ACTION_SUSPEND_ACCOUNT);
    }
    
    /**
     * Check if action involves customer account
     */
    public boolean involvesCustomerAccount() {
        return targetAccount != null;
    }
    
    @Override
    public String toString() {
        return "ActivityLog{" +
                "logId=" + logId +
                ", staffId=" + staffId +
                ", action='" + action + '\'' +
                ", targetAccount=" + targetAccount +
                ", timestamp=" + timestamp +
                '}';
    }
}
