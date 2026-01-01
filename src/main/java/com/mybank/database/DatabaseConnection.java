package com.mybank.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Connection Manager for Admin Module
 * Provides centralized database connection management
 */
public class DatabaseConnection {
    
    private static final String DB_URL = "jdbc:sqlite:database/mybank.db";
    private static Connection connection;
    
    /**
     * Get database connection
     * Creates new connection if none exists or if closed
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Use DatabaseHelper's connection if available
                Connection dbHelperConn = DatabaseHelper.getConnection();
                if (dbHelperConn != null && !dbHelperConn.isClosed()) {
                    return dbHelperConn;
                }
                
                // Otherwise create new connection
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                
                // Set pragmas for better performance
                Statement stmt = connection.createStatement();
                stmt.execute("PRAGMA busy_timeout = 5000");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.close();
            }
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
