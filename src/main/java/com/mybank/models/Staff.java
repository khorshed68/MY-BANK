package com.mybank.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing bank staff/employee information.
 * Used for staff authentication, role management, and activity tracking.
 */
public class Staff {
    private int staffId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private String profilePicturePath;
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;
    
    // Staff Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_OFFICER = "OFFICER";
    public static final String ROLE_TELLER = "TELLER";
    
    // Staff Status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_TERMINATED = "TERMINATED";
    
    // Constructors
    public Staff() {}
    
    public Staff(int staffId, String username, String fullName, String email, 
                 String phoneNumber, String role, String status) {
        this.staffId = staffId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
    }
    
    // Getters and Setters
    public int getStaffId() {
        return staffId;
    }
    
    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public String getProfilePicturePath() {
        return profilePicturePath;
    }
    
    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
    
    // Utility Methods
    
    /**
     * Check if staff member is active
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }
    
    /**
     * Check if staff member is suspended
     */
    public boolean isSuspended() {
        return STATUS_SUSPENDED.equals(status);
    }
    
    /**
     * Check if staff member is terminated
     */
    public boolean isTerminated() {
        return STATUS_TERMINATED.equals(status);
    }
    
    /**
     * Get formatted created date
     */
    public String getFormattedCreatedDate() {
        if (createdDate == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        return createdDate.format(formatter);
    }
    
    /**
     * Get formatted last login
     */
    public String getFormattedLastLogin() {
        if (lastLogin == null) return "Never";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        return lastLogin.format(formatter);
    }
    
    /**
     * Get role display name
     */
    public String getRoleDisplay() {
        switch (role) {
            case ROLE_ADMIN:
                return "Administrator";
            case ROLE_MANAGER:
                return "Manager";
            case ROLE_OFFICER:
                return "Banking Officer";
            case ROLE_TELLER:
                return "Teller";
            default:
                return role;
        }
    }
    
    /**
     * Get status display with color coding
     */
    public String getStatusDisplay() {
        return status;
    }
    
    /**
     * Check if staff has permission based on role hierarchy
     * ADMIN > MANAGER > OFFICER > TELLER
     */
    public boolean hasPermission(String requiredRole) {
        int currentLevel = getRoleLevel(this.role);
        int requiredLevel = getRoleLevel(requiredRole);
        return currentLevel >= requiredLevel;
    }
    
    /**
     * Get role level for hierarchy comparison
     */
    private int getRoleLevel(String role) {
        switch (role) {
            case ROLE_ADMIN:
                return 4;
            case ROLE_MANAGER:
                return 3;
            case ROLE_OFFICER:
                return 2;
            case ROLE_TELLER:
                return 1;
            default:
                return 0;
        }
    }
    
    @Override
    public String toString() {
        return "Staff{" +
                "staffId=" + staffId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
