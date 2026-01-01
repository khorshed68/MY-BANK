package com.mybank.database;

import com.mybank.models.AuditLog;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for AuditLog operations.
 */
public class AuditLogDAO {
    
    /**
     * Create the audit_logs table if it doesn't exist
     */
    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_type TEXT NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "action TEXT NOT NULL, " +
                    "module TEXT NOT NULL, " +
                    "details TEXT, " +
                    "ip_address TEXT, " +
                    "status TEXT DEFAULT 'SUCCESS', " +
                    "timestamp TEXT NOT NULL)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            // Create indexes for better query performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_type, user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Insert a new audit log entry
     */
    public static boolean insertLog(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_type, user_id, username, action, module, " +
                    "details, ip_address, status, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, log.getUserType());
            pstmt.setInt(2, log.getUserId());
            pstmt.setString(3, log.getUsername());
            pstmt.setString(4, log.getAction());
            pstmt.setString(5, log.getModule());
            pstmt.setString(6, log.getDetails());
            pstmt.setString(7, log.getIpAddress());
            pstmt.setString(8, log.getStatus());
            pstmt.setString(9, log.getTimestamp().toString());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all audit logs
     */
    public static List<AuditLog> getAllLogs() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 1000";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                logs.add(extractLogFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Get logs by user
     */
    public static List<AuditLog> getLogsByUser(String userType, int userId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_type = ? AND user_id = ? " +
                    "ORDER BY timestamp DESC LIMIT 500";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userType);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(extractLogFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Get logs by action
     */
    public static List<AuditLog> getLogsByAction(String action) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE action = ? ORDER BY timestamp DESC LIMIT 500";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, action);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(extractLogFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Get logs by date range
     */
    public static List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE timestamp BETWEEN ? AND ? " +
                    "ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(extractLogFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Get failed login attempts
     */
    public static List<AuditLog> getFailedLoginAttempts(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE action = 'LOGIN' AND status = 'FAILED' " +
                    "ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(extractLogFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    /**
     * Delete old logs (for maintenance)
     */
    public static boolean deleteOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        String sql = "DELETE FROM audit_logs WHERE timestamp < ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cutoffDate.toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Extract AuditLog from ResultSet
     */
    private static AuditLog extractLogFromResultSet(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("log_id"));
        log.setUserType(rs.getString("user_type"));
        log.setUserId(rs.getInt("user_id"));
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setModule(rs.getString("module"));
        log.setDetails(rs.getString("details"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setStatus(rs.getString("status"));
        
        String timestamp = rs.getString("timestamp");
        if (timestamp != null) {
            log.setTimestamp(LocalDateTime.parse(timestamp));
        }
        
        return log;
    }
}
