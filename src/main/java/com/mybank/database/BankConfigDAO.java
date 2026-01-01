package com.mybank.database;

import com.mybank.models.BankConfig;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for BankConfig operations.
 */
public class BankConfigDAO {
    
    /**
     * Create the bank_config table if it doesn't exist
     */
    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS bank_config (" +
                    "config_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "config_key TEXT UNIQUE NOT NULL, " +
                    "config_value TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "description TEXT, " +
                    "last_updated TEXT NOT NULL, " +
                    "updated_by TEXT)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            // Insert default configurations
            insertDefaultConfigs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Insert default bank configurations
     */
    private static void insertDefaultConfigs() {
        String checkSql = "SELECT COUNT(*) FROM bank_config";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert default configs
                insertConfig(new BankConfig("SAVINGS_INTEREST_RATE", "3.5", "INTEREST_RATE", 
                    "Annual interest rate for savings accounts (%)", "SYSTEM"));
                insertConfig(new BankConfig("CURRENT_INTEREST_RATE", "0.5", "INTEREST_RATE", 
                    "Annual interest rate for current accounts (%)", "SYSTEM"));
                insertConfig(new BankConfig("LOAN_INTEREST_RATE", "8.5", "INTEREST_RATE", 
                    "Annual interest rate for loans (%)", "SYSTEM"));
                insertConfig(new BankConfig("MIN_BALANCE_SAVINGS", "1000", "BALANCE", 
                    "Minimum balance for savings account", "SYSTEM"));
                insertConfig(new BankConfig("MIN_BALANCE_CURRENT", "5000", "BALANCE", 
                    "Minimum balance for current account", "SYSTEM"));
                insertConfig(new BankConfig("DAILY_WITHDRAWAL_LIMIT", "50000", "TRANSACTION_LIMIT", 
                    "Daily withdrawal limit", "SYSTEM"));
                insertConfig(new BankConfig("DAILY_TRANSFER_LIMIT", "100000", "TRANSACTION_LIMIT", 
                    "Daily transfer limit", "SYSTEM"));
                insertConfig(new BankConfig("MONTHLY_TRANSACTION_LIMIT", "500000", "TRANSACTION_LIMIT", 
                    "Monthly transaction limit", "SYSTEM"));
                insertConfig(new BankConfig("WITHDRAWAL_FEE", "10", "FEE", 
                    "Fee for ATM withdrawal", "SYSTEM"));
                insertConfig(new BankConfig("TRANSFER_FEE", "5", "FEE", 
                    "Fee for inter-account transfer", "SYSTEM"));
                insertConfig(new BankConfig("LOW_BALANCE_PENALTY", "100", "PENALTY", 
                    "Penalty for maintaining balance below minimum", "SYSTEM"));
                insertConfig(new BankConfig("SESSION_TIMEOUT_MINUTES", "15", "SECURITY", 
                    "Session timeout in minutes", "SYSTEM"));
                insertConfig(new BankConfig("MAX_LOGIN_ATTEMPTS", "3", "SECURITY", 
                    "Maximum failed login attempts before lockout", "SYSTEM"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Insert a new configuration
     */
    public static boolean insertConfig(BankConfig config) {
        String sql = "INSERT OR REPLACE INTO bank_config (config_key, config_value, category, " +
                    "description, last_updated, updated_by) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, config.getConfigKey());
            pstmt.setString(2, config.getConfigValue());
            pstmt.setString(3, config.getCategory());
            pstmt.setString(4, config.getDescription());
            pstmt.setString(5, LocalDateTime.now().toString());
            pstmt.setString(6, config.getUpdatedBy());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get configuration by key
     */
    public static BankConfig getConfigByKey(String key) {
        String sql = "SELECT * FROM bank_config WHERE config_key = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractConfigFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get configuration value by key
     */
    public static String getConfigValue(String key) {
        BankConfig config = getConfigByKey(key);
        return config != null ? config.getConfigValue() : null;
    }
    
    /**
     * Get all configurations
     */
    public static List<BankConfig> getAllConfigs() {
        List<BankConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM bank_config ORDER BY category, config_key";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                configs.add(extractConfigFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return configs;
    }
    
    /**
     * Get configurations by category
     */
    public static List<BankConfig> getConfigsByCategory(String category) {
        List<BankConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM bank_config WHERE category = ? ORDER BY config_key";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                configs.add(extractConfigFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return configs;
    }
    
    /**
     * Update configuration
     */
    public static boolean updateConfig(String key, String value, String updatedBy) {
        String sql = "UPDATE bank_config SET config_value = ?, last_updated = ?, " +
                    "updated_by = ? WHERE config_key = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, value);
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt.setString(3, updatedBy);
            pstmt.setString(4, key);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete configuration
     */
    public static boolean deleteConfig(String key) {
        String sql = "DELETE FROM bank_config WHERE config_key = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, key);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Extract BankConfig from ResultSet
     */
    private static BankConfig extractConfigFromResultSet(ResultSet rs) throws SQLException {
        BankConfig config = new BankConfig();
        config.setConfigId(rs.getInt("config_id"));
        config.setConfigKey(rs.getString("config_key"));
        config.setConfigValue(rs.getString("config_value"));
        config.setCategory(rs.getString("category"));
        config.setDescription(rs.getString("description"));
        
        String lastUpdated = rs.getString("last_updated");
        if (lastUpdated != null) {
            config.setLastUpdated(LocalDateTime.parse(lastUpdated));
        }
        
        config.setUpdatedBy(rs.getString("updated_by"));
        
        return config;
    }
}
