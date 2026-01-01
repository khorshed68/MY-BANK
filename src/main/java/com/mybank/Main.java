package com.mybank;

import com.mybank.models.Staff;
import com.mybank.services.StaffService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Application Class
 * Entry point for the My Bank application
 */
public class Main extends Application {
    
    private static Stage primaryStageObj;
    private static int loggedInAccountNumber = -1;
    private static Staff currentStaff = null;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStageObj = primaryStage;
            
            // Initialize default admin account
            StaffService.initializeDefaultAdmin();
            
            // Load Login FXML
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            
            // Create scene
            Scene scene = new Scene(root, 1200, 900);
            
            // Load CSS stylesheet
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            // Set up primary stage
            primaryStage.setTitle("MY BANK");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Changes the current scene
     * @param fxml The FXML file name (without path)
     * @throws Exception if loading fails
     */
    public static void changeScene(String fxml) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource("/fxml/" + fxml));
        Scene scene = new Scene(root, 1200, 900);
        scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
        primaryStageObj.setScene(scene);
    }
    
    /**
     * Main method
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Gets the logged-in account number
     * @return Account number or -1 if not logged in
     */
    public static int getLoggedInAccount() {
        return loggedInAccountNumber;
    }
    
    /**
     * Sets the logged-in account number
     * @param accountNumber The account number
     */
    public static void setLoggedInAccount(int accountNumber) {
        loggedInAccountNumber = accountNumber;
    }
    
    /**
     * Clears the logged-in account (for logout)
     */
    public static void clearLoggedInAccount() {
        loggedInAccountNumber = -1;
    }
    
    /**
     * Checks if a user is logged in
     * @return true if logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return loggedInAccountNumber > 0;
    }
    
    /**
     * Get current staff member
     * @return Current staff or null
     */
    public static Staff getCurrentStaff() {
        return currentStaff;
    }
    
    /**
     * Set current staff member
     * @param staff Staff object
     */
    public static void setCurrentStaff(Staff staff) {
        currentStaff = staff;
    }
    
    /**
     * Check if staff is logged in
     * @return true if staff logged in
     */
    public static boolean isStaffLoggedIn() {
        return currentStaff != null;
    }
    
    // Navigation methods for Staff Portal
    
    /**
     * Show staff login
     */
    public static void showStaffLogin() {
        try {
            changeScene("StaffLogin.fxml");
        } catch (Exception e) {
            System.err.println("Error loading staff login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show staff registration
     */
    public static void showStaffRegistration() {
        try {
            changeScene("StaffRegistration.fxml");
        } catch (Exception e) {
            System.err.println("Error loading staff registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show staff dashboard
     */
    public static void showStaffDashboard() {
        try {
            changeScene("StaffDashboard.fxml");
        } catch (Exception e) {
            System.err.println("Error loading staff dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show login screen
     */
    public static void showLogin() {
        try {
            changeScene("Login.fxml");
        } catch (Exception e) {
            System.err.println("Error loading login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show account approval
     */
    public static void showAccountApproval() {
        try {
            changeScene("AccountApproval.fxml");
        } catch (Exception e) {
            System.err.println("Error loading account approval: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show create account request
     */
    public static void showCreateAccountRequest() {
        try {
            changeScene("CreateAccount.fxml");
        } catch (Exception e) {
            System.err.println("Error loading create account request: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show customer management
     */
    public static void showCustomerManagement() {
        try {
            changeScene("CustomerManagement.fxml");
        } catch (Exception e) {
            System.err.println("Error loading customer management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show report generation
     */
    public static void showReportGeneration() {
        try {
            changeScene("ReportGeneration.fxml");
        } catch (Exception e) {
            System.err.println("Error loading report generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show activity log
     */
    public static void showActivityLog() {
        try {
            changeScene("ActivityLog.fxml");
        } catch (Exception e) {
            System.err.println("Error loading activity log: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show staff management
     */
    public static void showStaffManagement() {
        try {
            changeScene("StaffManagement.fxml");
        } catch (Exception e) {
            System.err.println("Error loading staff management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Admin Portal Navigation Methods
    
    /**
     * Show admin login
     */
    public static void showAdminLogin() {
        try {
            changeScene("AdminLogin.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin registration
     */
    public static void showAdminRegistration() {
        try {
            changeScene("AdminRegistration.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin dashboard
     */
    public static void showAdminDashboard() {
        try {
            changeScene("AdminDashboard.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin staff management
     */
    public static void showAdminStaffManagement() {
        try {
            changeScene("AdminStaffManagement.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin staff management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin customer oversight
     */
    public static void showAdminCustomerOversight() {
        try {
            changeScene("AdminCustomerOversight.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin customer oversight: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin transaction monitoring
     */
    public static void showAdminTransactionMonitoring() {
        try {
            changeScene("AdminTransactionMonitoring.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin transaction monitoring: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin bank configuration
     */
    public static void showAdminBankConfig() {
        try {
            changeScene("AdminBankConfig.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin bank config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin reports
     */
    public static void showAdminReports() {
        try {
            changeScene("AdminReports.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin reports: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin audit logs
     */
    public static void showAdminAuditLogs() {
        try {
            changeScene("AdminAuditLogs.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin audit logs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin system settings
     */
    public static void showAdminSystemSettings() {
        try {
            changeScene("AdminSystemSettings.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin system settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show admin management
     */
    public static void showAdminManagement() {
        try {
            changeScene("AdminManagement.fxml");
        } catch (Exception e) {
            System.err.println("Error loading admin management: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
