package com.mybank.controllers;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.models.Admin;
import com.mybank.services.AdminService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller for Admin Dashboard.
 * Displays overview statistics and quick access to admin functions.
 */
public class AdminDashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label totalCustomersLabel;
    
    @FXML
    private Label totalAccountsLabel;
    
    @FXML
    private Label totalStaffLabel;
    
    @FXML
    private Label totalAdminsLabel;
    
    @FXML
    private Label todayTransactionsLabel;
    
    @FXML
    private Label todayTransactionsAmountLabel;
    
    @FXML
    private Label activeAccountsLabel;
    
    @FXML
    private Label pendingApprovalsLabel;
    
    @FXML
    private Label suspiciousActivitiesLabel;
    
    @FXML
    private ImageView adminProfileImageView;
    
    private Admin currentAdmin;
    
    /**
     * Initialize the dashboard
     */
    @FXML
    private void initialize() {
        currentAdmin = AdminService.getCurrentAdmin();
        
        if (currentAdmin != null) {
            welcomeLabel.setText("Welcome, " + currentAdmin.getFullName());
            loadProfilePicture();
            loadStatistics();
        } else {
            // Session expired
            Main.showAdminLogin();
        }
    }
    
    /**
     * Load admin profile picture
     * Automatically centers and crops any image to fit the circular shape
     */
    private void loadProfilePicture() {
        if (adminProfileImageView != null) {
            // Load profile picture if available
            if (currentAdmin.getProfilePicturePath() != null && !currentAdmin.getProfilePicturePath().isEmpty()) {
                File imageFile = new File(currentAdmin.getProfilePicturePath());
                if (imageFile.exists()) {
                    try {
                        Image image = new Image(imageFile.toURI().toString());
                        adminProfileImageView.setImage(image);
                        
                        // Calculate viewport to center the image
                        double imageWidth = image.getWidth();
                        double imageHeight = image.getHeight();
                        double size = Math.min(imageWidth, imageHeight);
                        double offsetX = (imageWidth - size) / 2;
                        double offsetY = (imageHeight - size) / 2;
                        
                        // Set viewport to show center square portion
                        adminProfileImageView.setViewport(new javafx.geometry.Rectangle2D(offsetX, offsetY, size, size));
                        
                        // Set proper display settings
                        adminProfileImageView.setPreserveRatio(false);
                        adminProfileImageView.setFitWidth(56);
                        adminProfileImageView.setFitHeight(56);
                        adminProfileImageView.setSmooth(true);
                    } catch (Exception e) {
                        // Use default if loading fails
                        setDefaultProfileImage();
                    }
                } else {
                    setDefaultProfileImage();
                }
            } else {
                setDefaultProfileImage();
            }
        }
    }
    
    /**
     * Set default profile image (empty or placeholder)
     */
    private void setDefaultProfileImage() {
        // You can set a default image here if you have one
        adminProfileImageView.setImage(null);
    }
    
    /**
     * Load dashboard statistics
     */
    private void loadStatistics() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            Statement stmt = conn.createStatement();
            
            // Total Customers (unique account owners)
            ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT ownerName) FROM accounts");
            if (rs.next()) {
                totalCustomersLabel.setText(String.valueOf(rs.getInt(1)));
            }
            
            // Total Accounts
            rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts");
            if (rs.next()) {
                totalAccountsLabel.setText(String.valueOf(rs.getInt(1)));
            }
            
            // Total Staff
            rs = stmt.executeQuery("SELECT COUNT(*) FROM staff WHERE status = 'ACTIVE'");
            if (rs.next()) {
                totalStaffLabel.setText(String.valueOf(rs.getInt(1)));
            } else {
                totalStaffLabel.setText("0");
            }
            
            // Total Admins
            rs = stmt.executeQuery("SELECT COUNT(*) FROM admins WHERE status = 'ACTIVE'");
            if (rs.next()) {
                totalAdminsLabel.setText(String.valueOf(rs.getInt(1)));
            } else {
                totalAdminsLabel.setText("1");
            }
            
            // Today's Transactions
            rs = stmt.executeQuery(
                "SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE DATE(timestamp) = DATE('now')"
            );
            if (rs.next()) {
                todayTransactionsLabel.setText(String.valueOf(rs.getInt(1)));
                todayTransactionsAmountLabel.setText(String.format("৳ %.2f", rs.getDouble(2)));
            }
            
            // Active Accounts
            rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts WHERE status = 'ACTIVE'");
            if (rs.next()) {
                activeAccountsLabel.setText(String.valueOf(rs.getInt(1)));
            }
            
            // Pending Approvals
            rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts WHERE status = 'PENDING'");
            if (rs.next()) {
                pendingApprovalsLabel.setText(String.valueOf(rs.getInt(1)));
            } else {
                pendingApprovalsLabel.setText("0");
            }
            
            // Suspicious Activities (failed login attempts today)
            rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM audit_logs " +
                "WHERE action = 'LOGIN' AND status = 'FAILED' AND DATE(timestamp) = DATE('now')"
            );
            if (rs.next()) {
                suspiciousActivitiesLabel.setText(String.valueOf(rs.getInt(1)));
            } else {
                suspiciousActivitiesLabel.setText("0");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate to Staff Management
     */
    @FXML
    private void handleStaffManagement() {
        Main.showAdminStaffManagement();
    }
    
    /**
     * Navigate to Customer Oversight
     */
    @FXML
    private void handleCustomerOversight() {
        Main.showAdminCustomerOversight();
    }
    
    /**
     * Navigate to Transaction Monitoring
     */
    @FXML
    private void handleTransactionMonitoring() {
        Main.showAdminTransactionMonitoring();
    }
    
    /**
     * Navigate to Bank Configuration
     */
    @FXML
    private void handleBankConfig() {
        Main.showAdminBankConfig();
    }
    
    /**
     * Navigate to Reports & Analytics
     */
    @FXML
    private void handleReports() {
        Main.showAdminReports();
    }
    
    /**
     * Navigate to Audit Logs
     */
    @FXML
    private void handleAuditLogs() {
        Main.showAdminAuditLogs();
    }
    
    /**
     * Navigate to System Settings
     */
    @FXML
    private void handleSystemSettings() {
        Main.showAdminSystemSettings();
    }
    
    /**
     * Navigate to Admin Management
     */
    @FXML
    private void handleAdminManagement() {
        Main.showAdminManagement();
    }
    
    /**
     * Navigate to Cheque Oversight
     */
    @FXML
    private void handleChequeOversight() {
        try {
            Main.changeScene("AdminChequeOversight.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Cheque Oversight: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        AdminService.logout();
        Main.showAdminLogin();
    }
    
    /**
     * Refresh dashboard statistics
     */
    @FXML
    private void handleRefresh() {
        loadStatistics();
    }
}
