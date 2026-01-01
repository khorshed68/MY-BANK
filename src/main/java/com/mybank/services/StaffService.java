package com.mybank.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.mybank.database.DatabaseHelper;
import com.mybank.models.ActivityLog;
import com.mybank.models.Staff;

/**
 * Service class for staff authentication and management operations.
 * Handles staff login, role-based permissions, and activity logging.
 */
public class StaffService {
    
    /**
     * Authenticate staff member with username and password
     * @param username Staff username
     * @param password Plain text password
     * @return Staff object if authentication successful, null otherwise
     */
    public static Staff authenticateStaff(String username, String password) {
        String hashedPassword = hashPassword(password);
        Staff staff = null;
        
        String query = "SELECT * FROM staff WHERE username = ? AND passwordHash = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("status");
                
                // Check if staff account is active
                if (!Staff.STATUS_ACTIVE.equals(status)) {
                    System.err.println("Staff account is not active. Status: " + status);
                    return null;
                }
                
                staff = new Staff();
                staff.setStaffId(rs.getInt("staffId"));
                staff.setUsername(rs.getString("username"));
                staff.setPasswordHash(rs.getString("passwordHash"));
                staff.setFullName(rs.getString("fullName"));
                staff.setEmail(rs.getString("email"));
                staff.setPhoneNumber(rs.getString("phoneNumber"));
                staff.setRole(rs.getString("role"));
                staff.setStatus(status);
                staff.setProfilePicturePath(rs.getString("profilePicturePath"));
                
                Timestamp createdTs = rs.getTimestamp("createdDate");
                if (createdTs != null) {
                    staff.setCreatedDate(createdTs.toLocalDateTime());
                }
                
                Timestamp lastLoginTs = rs.getTimestamp("lastLogin");
                if (lastLoginTs != null) {
                    staff.setLastLogin(lastLoginTs.toLocalDateTime());
                }
                
                // Update last login timestamp
                updateLastLogin(staff.getStaffId());
                
                // Log successful login
                logActivity(staff.getStaffId(), ActivityLog.ACTION_LOGIN, null, 
                           "Staff " + username + " logged in successfully");
                
                System.out.println("Staff authentication successful: " + username);
            } else {
                System.err.println("Staff authentication failed for username: " + username);
            }
            
        } catch (SQLException e) {
            System.err.println("Error during staff authentication: " + e.getMessage());
            e.printStackTrace();
        }
        
        return staff;
    }
    
    /**
     * Create a new staff member
     * @param username Unique username
     * @param password Plain text password
     * @param fullName Full name
     * @param email Email address
     * @param phoneNumber Phone number
     * @param role Staff role
     * @param createdBy Staff ID of creator (for logging)
     * @param profilePicturePath Path to profile picture (can be null)
     * @return true if successful, false otherwise
     */
    public static boolean createStaff(String username, String password, String fullName, 
                                     String email, String phoneNumber, String role, int createdBy, String profilePicturePath) {
        String hashedPassword = hashPassword(password);
        
        String query = "INSERT INTO staff (username, passwordHash, fullName, email, phoneNumber, role, status, profilePicturePath) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, fullName);
            pstmt.setString(4, email);
            pstmt.setString(5, phoneNumber);
            pstmt.setString(6, role);
            
            // If self-registration (createdBy = 0), set status as PENDING for admin approval
            // If created by admin (createdBy > 0), set status as ACTIVE
            String status = (createdBy == 0) ? Staff.STATUS_PENDING : Staff.STATUS_ACTIVE;
            pstmt.setString(7, status);
            
            if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                pstmt.setString(8, profilePicturePath);
            } else {
                pstmt.setNull(8, Types.VARCHAR);
            }
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                String actionMsg = (createdBy == 0) 
                    ? "Staff self-registered (pending approval): " + username + " with role: " + role
                    : "Created new staff account: " + username + " with role: " + role;
                logActivity(createdBy, ActivityLog.ACTION_CREATE_ACCOUNT, null, actionMsg);
                System.out.println("Staff account created successfully: " + username + " (Status: " + status + ")");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating staff account: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get staff information by staff ID
     */
    public static Staff getStaffById(int staffId) {
        String query = "SELECT * FROM staff WHERE staffId = ?";
        Staff staff = null;
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                staff = new Staff();
                staff.setStaffId(rs.getInt("staffId"));
                staff.setUsername(rs.getString("username"));
                staff.setFullName(rs.getString("fullName"));
                staff.setEmail(rs.getString("email"));
                staff.setPhoneNumber(rs.getString("phoneNumber"));
                staff.setRole(rs.getString("role"));
                staff.setStatus(rs.getString("status"));
                staff.setProfilePicturePath(rs.getString("profilePicturePath"));
                
                Timestamp createdTs = rs.getTimestamp("createdDate");
                if (createdTs != null) {
                    staff.setCreatedDate(createdTs.toLocalDateTime());
                }
                
                Timestamp lastLoginTs = rs.getTimestamp("lastLogin");
                if (lastLoginTs != null) {
                    staff.setLastLogin(lastLoginTs.toLocalDateTime());
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving staff information: " + e.getMessage());
            e.printStackTrace();
        }
        
        return staff;
    }
    
    /**
     * Check if staff has required permission
     * @param staffId Staff ID
     * @param requiredRole Minimum required role
     * @return true if staff has permission, false otherwise
     */
    public static boolean hasPermission(int staffId, String requiredRole) {
        Staff staff = getStaffById(staffId);
        if (staff == null) return false;
        
        return staff.hasPermission(requiredRole);
    }
    
    /**
     * Log staff activity
     */
    public static void logActivity(int staffId, String action, Integer targetAccount, String details) {
        String query = "INSERT INTO staff_activity_log (staffId, action, targetAccount, details) " +
                      "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, staffId);
            pstmt.setString(2, action);
            if (targetAccount != null) {
                pstmt.setInt(3, targetAccount);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setString(4, details);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error logging staff activity: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get activity logs for a specific staff member
     */
    public static List<ActivityLog> getStaffActivityLogs(int staffId, int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        
        String query = "SELECT l.*, s.fullName FROM staff_activity_log l " +
                      "JOIN staff s ON l.staffId = s.staffId " +
                      "WHERE l.staffId = ? " +
                      "ORDER BY l.timestamp DESC LIMIT ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, staffId);
            pstmt.setInt(2, limit);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("logId"));
                log.setStaffId(rs.getInt("staffId"));
                log.setStaffName(rs.getString("fullName"));
                log.setAction(rs.getString("action"));
                
                int targetAccount = rs.getInt("targetAccount");
                if (!rs.wasNull()) {
                    log.setTargetAccount(targetAccount);
                }
                
                log.setDetails(rs.getString("details"));
                
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) {
                    log.setTimestamp(ts.toLocalDateTime());
                }
                
                log.setIpAddress(rs.getString("ipAddress"));
                
                logs.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving activity logs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    /**
     * Get all activity logs (for admin)
     */
    public static List<ActivityLog> getAllActivityLogs(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        
        String query = "SELECT l.*, s.fullName FROM staff_activity_log l " +
                      "JOIN staff s ON l.staffId = s.staffId " +
                      "ORDER BY l.timestamp DESC LIMIT ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, limit);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("logId"));
                log.setStaffId(rs.getInt("staffId"));
                log.setStaffName(rs.getString("fullName"));
                log.setAction(rs.getString("action"));
                
                int targetAccount = rs.getInt("targetAccount");
                if (!rs.wasNull()) {
                    log.setTargetAccount(targetAccount);
                }
                
                log.setDetails(rs.getString("details"));
                
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) {
                    log.setTimestamp(ts.toLocalDateTime());
                }
                
                log.setIpAddress(rs.getString("ipAddress"));
                
                logs.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving activity logs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    /**
     * Update staff last login timestamp
     */
    private static void updateLastLogin(int staffId) {
        String query = "UPDATE staff SET lastLogin = CURRENT_TIMESTAMP WHERE staffId = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, staffId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Hash password using SHA-256
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Initialize default admin account if not exists
     */
    public static void initializeDefaultAdmin() {
        String checkQuery = "SELECT COUNT(*) FROM staff WHERE role = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            
            pstmt.setString(1, Staff.ROLE_ADMIN);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) == 0) {
                // No admin exists, create default admin
                String insertQuery = "INSERT INTO staff (username, passwordHash, fullName, email, phoneNumber, role, status) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, "admin");
                    insertStmt.setString(2, hashPassword("admin123"));
                    insertStmt.setString(3, "System Administrator");
                    insertStmt.setString(4, "admin@mybank.com");
                    insertStmt.setString(5, "01700000000");
                    insertStmt.setString(6, Staff.ROLE_ADMIN);
                    insertStmt.setString(7, Staff.STATUS_ACTIVE);
                    
                    insertStmt.executeUpdate();
                    System.out.println("Default admin account created (username: admin, password: admin123)");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error initializing default admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
