package com.mybank.services;

import com.mybank.models.Admin;
import com.mybank.models.AuditLog;
import com.mybank.database.AdminDAO;
import com.mybank.database.AuditLogDAO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * Service class for Admin operations with security and audit logging.
 */
public class AdminService {
    
    private static Admin currentAdmin = null;
    private static LocalDateTime lastActivity = null;
    private static final int SESSION_TIMEOUT_MINUTES = 15;
    
    /**
     * Initialize admin tables
     */
    public static void initialize() {
        AdminDAO.createTable();
        AuditLogDAO.createTable();
        
        // Create default super admin if no admins exist
        createDefaultSuperAdmin();
    }
    
    /**
     * Create default super admin account
     */
    private static void createDefaultSuperAdmin() {
        if (AdminDAO.getAllAdmins().isEmpty()) {
            Admin superAdmin = new Admin();
            superAdmin.setUsername("admin");
            superAdmin.setPassword(hashPassword("admin123"));
            superAdmin.setFullName("System Administrator");
            superAdmin.setEmail("admin@mybank.com");
            superAdmin.setPhone("01234567890");
            superAdmin.setStatus("ACTIVE");
            superAdmin.setCreatedBy("SYSTEM");
            superAdmin.setSuperAdmin(true);
            
            AdminDAO.insertAdmin(superAdmin);
        }
    }
    
    /**
     * Authenticate admin
     */
    public static boolean authenticate(String username, String password) {
        try {
            Admin admin = AdminDAO.findByUsername(username);
            
            if (admin == null) {
                logAuditEvent("ADMIN", 0, username, "LOGIN", "ADMIN", 
                    "Failed login attempt - user not found", "FAILED");
                return false;
            }
            
            if (!admin.getStatus().equals("ACTIVE")) {
                logAuditEvent("ADMIN", admin.getAdminId(), username, "LOGIN", "ADMIN", 
                    "Failed login attempt - account not active", "FAILED");
                return false;
            }
            
            String hashedPassword = hashPassword(password);
            if (!admin.getPassword().equals(hashedPassword)) {
                logAuditEvent("ADMIN", admin.getAdminId(), username, "LOGIN", "ADMIN", 
                    "Failed login attempt - incorrect password", "FAILED");
                return false;
            }
            
            // Successful login
            currentAdmin = admin;
            lastActivity = LocalDateTime.now();
            AdminDAO.updateLastLogin(admin.getAdminId());
            
            logAuditEvent("ADMIN", admin.getAdminId(), username, "LOGIN", "ADMIN", 
                "Successful login", "SUCCESS");
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Logout admin
     */
    public static void logout() {
        if (currentAdmin != null) {
            logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(), 
                "LOGOUT", "ADMIN", "Admin logged out", "SUCCESS");
            currentAdmin = null;
            lastActivity = null;
        }
    }
    
    /**
     * Check if session is active
     */
    public static boolean isSessionActive() {
        if (currentAdmin == null || lastActivity == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesInactive = java.time.Duration.between(lastActivity, now).toMinutes();
        
        if (minutesInactive >= SESSION_TIMEOUT_MINUTES) {
            logout();
            return false;
        }
        
        // Update last activity
        lastActivity = now;
        return true;
    }
    
    /**
     * Get current admin
     */
    public static Admin getCurrentAdmin() {
        if (isSessionActive()) {
            return currentAdmin;
        }
        return null;
    }
    
    /**
     * Create new admin
     */
    public static boolean createAdmin(String username, String password, String fullName, 
                                     String email, String phone, boolean isSuperAdmin) {
        if (currentAdmin == null) {
            return false;
        }
        
        // Check if username already exists
        if (AdminDAO.usernameExists(username)) {
            return false;
        }
        
        Admin newAdmin = new Admin();
        newAdmin.setUsername(username);
        newAdmin.setPassword(hashPassword(password));
        newAdmin.setFullName(fullName);
        newAdmin.setEmail(email);
        newAdmin.setPhone(phone);
        newAdmin.setStatus("ACTIVE");
        newAdmin.setCreatedBy(currentAdmin.getUsername());
        newAdmin.setSuperAdmin(isSuperAdmin);
        
        boolean success = AdminDAO.insertAdmin(newAdmin);
        
        if (success) {
            logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(), 
                "CREATE", "ADMIN", "Created new admin: " + username, "SUCCESS");
        }
        
        return success;
    }
    
    /**
     * Update admin
     */
    public static boolean updateAdmin(int adminId, String fullName, String email, String phone) {
        if (currentAdmin == null) {
            return false;
        }
        
        Admin admin = AdminDAO.findById(adminId);
        if (admin == null) {
            return false;
        }
        
        admin.setFullName(fullName);
        admin.setEmail(email);
        admin.setPhone(phone);
        
        boolean success = AdminDAO.updateAdmin(admin);
        
        if (success) {
            logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(), 
                "UPDATE", "ADMIN", "Updated admin: " + admin.getUsername(), "SUCCESS");
        }
        
        return success;
    }
    
    /**
     * Change admin password
     */
    public static boolean changePassword(int adminId, String oldPassword, String newPassword) {
        if (currentAdmin == null) {
            return false;
        }
        
        Admin admin = AdminDAO.findById(adminId);
        if (admin == null) {
            return false;
        }
        
        // Verify old password
        String hashedOldPassword = hashPassword(oldPassword);
        if (!admin.getPassword().equals(hashedOldPassword)) {
            logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(), 
                "PASSWORD_CHANGE", "ADMIN", "Failed password change - incorrect old password", "FAILED");
            return false;
        }
        
        String hashedNewPassword = hashPassword(newPassword);
        boolean success = AdminDAO.updatePassword(adminId, hashedNewPassword);
        
        if (success) {
            logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(), 
                "PASSWORD_CHANGE", "ADMIN", "Successfully changed password", "SUCCESS");
        }
        
        return success;
    }
    
    /**
     * Reset admin password (by super admin)
     */
    public static boolean resetPassword(int adminId, String newPassword) {
        if (currentAdmin == null || !currentAdmin.isSuperAdmin()) {
            return false;
        }
        
        Admin admin = AdminDAO.findById(adminId);
        if (admin == null) {
            return false;
        }
        
        String hashedPassword = hashPassword(newPassword);
        boolean success = AdminDAO.updatePassword(adminId, hashedPassword);
        
        if (success) {
            logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(), 
                "PASSWORD_RESET", "ADMIN", "Reset password for admin: " + admin.getUsername(), "SUCCESS");
        }
        
        return success;
    }
    
    /**
     * Activate/Deactivate admin
     */
    public static boolean setAdminStatus(int adminId, String status) {
        if (currentAdmin == null || !currentAdmin.isSuperAdmin()) {
            return false;
        }
        
        Admin admin = AdminDAO.findById(adminId);
        if (admin == null) {
            return false;
        }
        
        admin.setStatus(status);
        boolean success = AdminDAO.updateAdmin(admin);
        
        if (success) {
            logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(), 
                "STATUS_CHANGE", "ADMIN", "Changed status to " + status + " for admin: " + admin.getUsername(), "SUCCESS");
        }
        
        return success;
    }
    
    /**
     * Delete admin
     */
    public static boolean deleteAdmin(int adminId) {
        if (currentAdmin == null || !currentAdmin.isSuperAdmin()) {
            return false;
        }
        
        // Cannot delete self
        if (adminId == currentAdmin.getAdminId()) {
            return false;
        }
        
        Admin admin = AdminDAO.findById(adminId);
        if (admin == null) {
            return false;
        }
        
        boolean success = AdminDAO.deleteAdmin(adminId);
        
        if (success) {
            logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(), 
                "DELETE", "ADMIN", "Deleted admin: " + admin.getUsername(), "SUCCESS");
        }
        
        return success;
    }
    
    /**
     * Get all admins
     */
    public static List<Admin> getAllAdmins() {
        if (currentAdmin == null) {
            return null;
        }
        return AdminDAO.getAllAdmins();
    }
    
    /**
     * Hash password using SHA-256
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback (not recommended for production)
        }
    }
    
    /**
     * Log audit event
     */
    public static void logAuditEvent(String userType, int userId, String username, 
                                    String action, String module, String details, String status) {
        AuditLog log = new AuditLog();
        log.setUserType(userType);
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setModule(module);
        log.setDetails(details);
        log.setIpAddress("127.0.0.1"); // Could be enhanced with actual IP tracking
        log.setStatus(status);
        
        AuditLogDAO.insertLog(log);
    }
    
    /**
     * Get audit logs
     */
    public static List<AuditLog> getAuditLogs() {
        if (currentAdmin == null) {
            return null;
        }
        return AuditLogDAO.getAllLogs();
    }
    
    /**
     * Get audit logs by user
     */
    public static List<AuditLog> getAuditLogsByUser(String userType, int userId) {
        if (currentAdmin == null) {
            return null;
        }
        return AuditLogDAO.getLogsByUser(userType, userId);
    }
    
    /**
     * Get failed login attempts
     */
    public static List<AuditLog> getFailedLoginAttempts() {
        if (currentAdmin == null) {
            return null;
        }
        return AuditLogDAO.getFailedLoginAttempts(100);
    }
}
