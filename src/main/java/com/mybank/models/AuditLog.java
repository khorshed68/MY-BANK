package com.mybank.models;

import java.time.LocalDateTime;

/**
 * AuditLog model for tracking all administrative and system activities.
 */
public class AuditLog {
    private int logId;
    private String userType; // ADMIN, STAFF, CUSTOMER, SYSTEM
    private int userId;
    private String username;
    private String action; // LOGIN, LOGOUT, CREATE, UPDATE, DELETE, VIEW, TRANSFER, etc.
    private String module; // ADMIN, STAFF, CUSTOMER, TRANSACTION, ACCOUNT, etc.
    private String details; // JSON or text description of the action
    private String ipAddress;
    private String status; // SUCCESS, FAILED, WARNING
    private LocalDateTime timestamp;
    
    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
        this.status = "SUCCESS";
    }
    
    public AuditLog(String userType, int userId, String username, String action, 
                    String module, String details, String ipAddress, String status) {
        this.userType = userType;
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.module = module;
        this.details = details;
        this.ipAddress = ipAddress;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getLogId() {
        return logId;
    }
    
    public void setLogId(int logId) {
        this.logId = logId;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getModule() {
        return module;
    }
    
    public void setModule(String module) {
        this.module = module;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", userType='" + userType + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                ", module='" + module + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
