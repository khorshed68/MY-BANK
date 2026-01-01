package com.mybank.models;

import java.time.LocalDateTime;

/**
 * Admin model representing administrative users with full system privileges.
 */
public class Admin {
    private int adminId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String status; // ACTIVE, INACTIVE, SUSPENDED
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;
    private String createdBy;
    private boolean isSuperAdmin; // Super admin has additional privileges
    private String profilePicturePath; // Path to profile picture
    
    // Constructors
    public Admin() {
        this.status = "ACTIVE";
        this.createdDate = LocalDateTime.now();
        this.isSuperAdmin = false;
    }
    
    public Admin(int adminId, String username, String password, String fullName, 
                 String email, String phone, String status, LocalDateTime createdDate,
                 LocalDateTime lastLogin, String createdBy, boolean isSuperAdmin) {
        this.adminId = adminId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.createdDate = createdDate;
        this.lastLogin = lastLogin;
        this.createdBy = createdBy;
        this.isSuperAdmin = isSuperAdmin;
    }
    
    public Admin(int adminId, String username, String password, String fullName, 
                 String email, String phone, String status, LocalDateTime createdDate,
                 LocalDateTime lastLogin, String createdBy, boolean isSuperAdmin, String profilePicturePath) {
        this(adminId, username, password, fullName, email, phone, status, createdDate, lastLogin, createdBy, isSuperAdmin);
        this.profilePicturePath = profilePicturePath;
    }
    
    // Getters and Setters
    public int getAdminId() {
        return adminId;
    }
    
    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
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
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
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
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }
    
    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }
    
    public String getProfilePicturePath() {
        return profilePicturePath;
    }
    
    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                ", isSuperAdmin=" + isSuperAdmin +
                '}';
    }
}
