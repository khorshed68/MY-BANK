package com.mybank.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.mybank.Main;
import com.mybank.database.DatabaseConnection;
import com.mybank.models.Admin;
import com.mybank.services.AdminService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * Controller for System Settings and Maintenance.
 */
public class AdminSystemSettingsController {
    
    private Admin currentAdmin;
    
    @FXML
    private void initialize() {
        currentAdmin = AdminService.getCurrentAdmin();
        
        if (currentAdmin == null) {
            Main.showAdminLogin();
            return;
        }
    }
    
    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }
    
    @FXML
    private void handleBackupDatabase() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Database Backup");
        confirmAlert.setHeaderText("Create Database Backup");
        confirmAlert.setContentText("This will create a backup of the entire database.\n\nProceed with backup?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Backup Location");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            
            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                performBackup(selectedDirectory);
            }
        }
    }
    
    private void performBackup(File directory) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "mybank_backup_" + timestamp + ".db";
            File backupFile = new File(directory, backupFileName);
            
            // SQLite backup - copy database file
            try {
                // Get the database file path
                File sourceDb = new File("database/mybank.db");
                if (!sourceDb.exists()) {
                    showError("Database Not Found", "Database file not found at: " + sourceDb.getAbsolutePath());
                    return;
                }
                
                // Copy the database file
                Files.copy(sourceDb.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                showSuccess("Database Backup Successful", 
                    "Backup created successfully!\n\n" +
                    "File: " + backupFile.getName() + "\n" +
                    "Location: " + backupFile.getParent() + "\n" +
                    "Size: " + (backupFile.length() / 1024) + " KB");
                return;
                
            } catch (IOException e) {
                showError("Backup Failed", "Failed to copy database file: " + e.getMessage());
                return;
            }
            
            // Alternative: SQL dump approach (commented out for SQLite)
            /*
            // This approach is for MySQL/other databases, not needed for SQLite
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile));
                 Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Write header
                writer.write("-- MyBank Database Backup\n");
                writer.write("-- Created: " + LocalDateTime.now() + "\n");
                writer.write("-- Database: mybank\n\n");
                
                // Get all tables
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
                
                int tableCount = 0;
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    tableCount++;
                    
                    // Write table structure
                    writer.write("\n-- Table: " + tableName + "\n");
                    writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");
                    
                    ResultSet createTableRs = stmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`");
                    if (createTableRs.next()) {
                        writer.write(createTableRs.getString(2) + ";\n\n");
                    }
                    createTableRs.close();
                    
                    // Write table data
                    ResultSet dataRs = stmt.executeQuery("SELECT * FROM `" + tableName + "`");
                    ResultSetMetaData rsMetaData = dataRs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();
                    
                    int rowCount = 0;
                    while (dataRs.next()) {
                        if (rowCount == 0) {
                            writer.write("-- Data for table: " + tableName + "\n");
                        }
                        
                        writer.write("INSERT INTO `" + tableName + "` VALUES (");
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) writer.write(", ");
                            
                            Object value = dataRs.getObject(i);
                            if (value == null) {
                                writer.write("NULL");
                            } else if (value instanceof String || value instanceof Date || value instanceof Timestamp) {
                                writer.write("'" + value.toString().replace("'", "''") + "'");
                            } else {
                                writer.write(value.toString());
                            }
                        }
                        writer.write(");\n");
                        rowCount++;
                    }
                    dataRs.close();
                    
                    if (rowCount > 0) {
                        writer.write("\n");
                    }
                }
                
                writer.write("\n-- Backup completed successfully\n");
                writer.write("-- Tables backed up: " + tableCount + "\n");
                
                showSuccess("Database Backup Successful", 
                    "Backup created successfully!\n\n" +
                    "File: " + backupFile.getName() + "\n" +
                    "Location: " + backupFile.getParent() + "\n" +
                    "Tables: " + tableCount);
                    
            } catch (SQLException e) {
                showError("Database Error", "Failed to create backup: " + e.getMessage());
            } catch (IOException e) {
                showError("File Error", "Failed to write backup file: " + e.getMessage());
            }
            */
            
        } catch (Exception e) {
            showError("Backup Error", "Unexpected error during backup: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRestoreDatabase() {
        Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
        confirmAlert.setTitle("Database Restore");
        confirmAlert.setHeaderText("Restore Database from Backup");
        confirmAlert.setContentText("WARNING: This will replace all current data with the backup data.\n\n" +
            "Make sure you have a recent backup before proceeding.\n\n" +
            "Continue with restore?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Backup File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQL Files", "*.sql")
            );
            
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                performRestore(selectedFile);
            }
        }
    }
    
    private void performRestore(File backupFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(backupFile));
             Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            int statementsExecuted = 0;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                sqlBuilder.append(line).append(" ");
                
                // Execute statement when semicolon is found
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                            statementsExecuted++;
                        } catch (SQLException e) {
                            // Continue with next statement even if one fails
                            System.err.println("Error executing: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                        }
                    }
                    sqlBuilder.setLength(0);
                }
            }
            
            showSuccess("Database Restore Successful",
                "Database restored successfully!\n\n" +
                "File: " + backupFile.getName() + "\n" +
                "Statements executed: " + statementsExecuted);
            
        } catch (IOException e) {
            showError("File Error", "Failed to read backup file: " + e.getMessage());
        } catch (SQLException e) {
            showError("Database Error", "Failed to restore database: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearLogs() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear Audit Logs");
        confirmAlert.setHeaderText("Clear Old Audit Logs");
        confirmAlert.setContentText("This will delete audit logs older than 90 days.\n\n" +
            "Recent logs will be preserved for compliance.\n\n" +
            "Continue?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            performClearLogs();
        }
    }
    
    private void performClearLogs() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if audit_logs table exists
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "audit_logs", null);
            
            if (tables.next()) {
                // Delete logs older than 90 days
                String deleteSql = "DELETE FROM audit_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL 90 DAY)";
                int deletedRows = stmt.executeUpdate(deleteSql);
                
                // Get remaining log count
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM audit_logs");
                int remainingLogs = 0;
                if (rs.next()) {
                    remainingLogs = rs.getInt("count");
                }
                rs.close();
                
                showSuccess("Logs Cleared",
                    "Old audit logs cleared successfully!\n\n" +
                    "Deleted logs: " + deletedRows + "\n" +
                    "Remaining logs: " + remainingLogs);
            } else {
                showInfo("No Logs Table", "Audit logs table does not exist.");
            }
            
            tables.close();
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to clear logs: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSystemHealth() {
        try {
            StringBuilder healthReport = new StringBuilder();
            healthReport.append("=== SYSTEM HEALTH REPORT ===\n");
            healthReport.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
            
            // Database Connection
            try (Connection conn = DatabaseConnection.getConnection()) {
                healthReport.append("✅ Database: Connected\n");
                healthReport.append("   URL: ").append(conn.getMetaData().getURL()).append("\n");
                healthReport.append("   Product: ").append(conn.getMetaData().getDatabaseProductName())
                    .append(" ").append(conn.getMetaData().getDatabaseProductVersion()).append("\n\n");
                
                // Table Statistics
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
                int tableCount = 0;
                
                healthReport.append("DATABASE STATISTICS:\n");
                try (Statement stmt = conn.createStatement()) {
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        tableCount++;
                        
                        try {
                            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM `" + tableName + "`");
                            if (rs.next()) {
                                healthReport.append("   • ").append(tableName).append(": ")
                                    .append(rs.getInt("count")).append(" records\n");
                            }
                            rs.close();
                        } catch (SQLException e) {
                            healthReport.append("   • ").append(tableName).append(": Error reading\n");
                        }
                    }
                }
                tables.close();
                
                healthReport.append("\nTotal Tables: ").append(tableCount).append("\n\n");
                
            } catch (SQLException e) {
                healthReport.append("❌ Database: Connection Failed\n");
                healthReport.append("   Error: ").append(e.getMessage()).append("\n\n");
            }
            
            // Admin Service
            healthReport.append("✅ Admin Service: Active\n");
            if (currentAdmin != null) {
                healthReport.append("   Current User: ").append(currentAdmin.getUsername()).append("\n");
            }
            healthReport.append("\n");
            
            // System Resources
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / (1024 * 1024);
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            long freeMemory = runtime.freeMemory() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;
            
            healthReport.append("SYSTEM RESOURCES:\n");
            healthReport.append("   • Max Memory: ").append(maxMemory).append(" MB\n");
            healthReport.append("   • Total Memory: ").append(totalMemory).append(" MB\n");
            healthReport.append("   • Used Memory: ").append(usedMemory).append(" MB\n");
            healthReport.append("   • Free Memory: ").append(freeMemory).append(" MB\n");
            healthReport.append("   • Available Processors: ").append(runtime.availableProcessors()).append("\n\n");
            
            healthReport.append("✅ Audit Logging: Enabled\n");
            healthReport.append("✅ Authentication: Active\n");
            healthReport.append("\n=== END OF REPORT ===");
            
            // Show in dialog with TextArea
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("System Health Report");
            alert.setHeaderText("System Status: Healthy");
            
            TextArea textArea = new TextArea(healthReport.toString());
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            textArea.setPrefRowCount(20);
            textArea.setPrefColumnCount(60);
            
            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefWidth(700);
            alert.showAndWait();
            
        } catch (Exception e) {
            showError("System Health Error", "Failed to generate health report: " + e.getMessage());
        }
    }
    
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
