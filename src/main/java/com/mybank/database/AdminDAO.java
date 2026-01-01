package com.mybank.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mybank.models.Admin;

/**
 * Data Access Object for Admin operations.
 */
public class AdminDAO {
    
    /**
     * Create the admins table if it doesn't exist
     */
    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS admins (" +
                    "admin_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "phone TEXT, " +
                    "status TEXT DEFAULT 'ACTIVE', " +
                    "created_date TEXT NOT NULL, " +
                    "last_login TEXT, " +
                    "created_by TEXT, " +
                    "is_super_admin INTEGER DEFAULT 0, " +
                    "profile_picture_path TEXT)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            // Add column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE admins ADD COLUMN profile_picture_path TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Insert a new admin
     */
    public static boolean insertAdmin(Admin admin) {
        String sql = "INSERT INTO admins (username, password, full_name, email, phone, " +
                    "status, created_date, created_by, is_super_admin, profile_picture_path) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, admin.getUsername());
            pstmt.setString(2, admin.getPassword());
            pstmt.setString(3, admin.getFullName());
            pstmt.setString(4, admin.getEmail());
            pstmt.setString(5, admin.getPhone());
            pstmt.setString(6, admin.getStatus());
            pstmt.setString(7, admin.getCreatedDate().toString());
            pstmt.setString(8, admin.getCreatedBy());
            pstmt.setInt(9, admin.isSuperAdmin() ? 1 : 0);
            pstmt.setString(10, admin.getProfilePicturePath());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Find admin by username
     */
    public static Admin findByUsername(String username) {
        String sql = "SELECT * FROM admins WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractAdminFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Find admin by ID
     */
    public static Admin findById(int adminId) {
        String sql = "SELECT * FROM admins WHERE admin_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, adminId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractAdminFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Find admin by email
     */
    public static Admin findByEmail(String email) {
        String sql = "SELECT * FROM admins WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractAdminFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all admins
     */
    public static List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM admins ORDER BY created_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                admins.add(extractAdminFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return admins;
    }
    
    /**
     * Update admin
     */
    public static boolean updateAdmin(Admin admin) {
        String sql = "UPDATE admins SET full_name = ?, email = ?, phone = ?, " +
                    "status = ? WHERE admin_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, admin.getFullName());
            pstmt.setString(2, admin.getEmail());
            pstmt.setString(3, admin.getPhone());
            pstmt.setString(4, admin.getStatus());
            pstmt.setInt(5, admin.getAdminId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update password
     */
    public static boolean updatePassword(int adminId, String newPassword) {
        String sql = "UPDATE admins SET password = ? WHERE admin_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, adminId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update last login time
     */
    public static boolean updateLastLogin(int adminId) {
        String sql = "UPDATE admins SET last_login = ? WHERE admin_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, adminId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete admin
     */
    public static boolean deleteAdmin(int adminId) {
        String sql = "DELETE FROM admins WHERE admin_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, adminId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if username exists
     */
    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM admins WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Extract Admin from ResultSet
     */
    private static Admin extractAdminFromResultSet(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setAdminId(rs.getInt("admin_id"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setFullName(rs.getString("full_name"));
        admin.setEmail(rs.getString("email"));
        admin.setPhone(rs.getString("phone"));
        admin.setStatus(rs.getString("status"));
        
        String createdDate = rs.getString("created_date");
        if (createdDate != null) {
            admin.setCreatedDate(LocalDateTime.parse(createdDate));
        }
        
        String lastLogin = rs.getString("last_login");
        if (lastLogin != null && !lastLogin.isEmpty()) {
            admin.setLastLogin(LocalDateTime.parse(lastLogin));
        }
        
        admin.setCreatedBy(rs.getString("created_by"));
        admin.setSuperAdmin(rs.getInt("is_super_admin") == 1);
        admin.setProfilePicturePath(rs.getString("profile_picture_path"));
        
        return admin;
    }
}
