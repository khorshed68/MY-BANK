package com.mybank.controllers;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.services.NotificationService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Login Controller
 * Handles user authentication and login
 * 2. Returning user: Phone Number + Password
 */
public class LoginController {
    
    @FXML
    private TextField phoneNumberField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label messageLabel;
    
    private DatabaseHelper dbHelper;
    private NotificationService notificationService;
    
    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        dbHelper = new DatabaseHelper();
        notificationService = new NotificationService();
    }
    

    
    /**
     * Handles login button click
     */
    @FXML
    private void handleLogin() {
        // Clear previous message
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: black;");
        
        try {
            // Always use phone number + password login
            handleUserLogin();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles user login with phone number and password
     */
    private void handleUserLogin() {
        // Validate inputs
        if (phoneNumberField.getText().trim().isEmpty()) {
            showError("Please enter your phone number!");
            return;
        }
        
        if (passwordField.getText().trim().isEmpty()) {
            showError("Please enter your password!");
            return;
        }
        
        String phoneNumber = phoneNumberField.getText().trim();
        String password = passwordField.getText();
        
        // Get account number by phone
        int accountNumber = dbHelper.getAccountByPhone(phoneNumber);
        
        if (accountNumber <= 0) {
            showError("Account not found with this phone number!");
            return;
        }
        
        // Check if account has a password set
        if (!dbHelper.hasPassword(accountNumber)) {
            showError("Password not set for this account. Please contact support.");
            return;
        }
        
        // Check account status
        String status = dbHelper.getAccountStatus(accountNumber);
        if ("BLOCKED".equals(status)) {
            showError("🔒 ACCOUNT BLOCKED\n\n" +
                     "Your account was blocked after 3 failed login attempts.\n\n" +
                     "TO UNBLOCK YOUR ACCOUNT:\n" +
                     "1. Check your email for account recovery instructions\n" +
                     "2. Contact Bank Support:\n" +
                     "   📧 Email: support@mybank.com\n" +
                     "   📞 Phone: 1-800-MYBANK-HELP\n" +
                     "3. Visit any MY BANK branch with your ID\n\n" +
                     "Our staff will verify your identity and unblock your account.");
            return;
        }
        
        // Attempt authentication
        boolean authenticated = dbHelper.authenticateLogin(accountNumber, password);
        
        if (authenticated) {
            // Store logged-in account number
            Main.setLoggedInAccount(accountNumber);
            
            // Send login notification
            new Thread(() -> {
                notificationService.sendLoginNotification(accountNumber);
            }).start();
            
            // Check if user is still using default password
            if (dbHelper.isUsingDefaultPassword(accountNumber)) {
                showSuccess("Login successful! You must change your password...");
                
                // Force password change for default password users
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                showPasswordSetupDialog(accountNumber, true); // true = mandatory
                            } catch (Exception e) {
                                System.err.println("Error showing password setup: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                showSuccess("Login successful! Redirecting...");
                
                // Small delay before redirect
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                Main.changeScene("Dashboard.fxml");
                            } catch (Exception e) {
                                System.err.println("Error redirecting to dashboard: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            
        } else {
            int failedAttempts = dbHelper.getFailedAttempts(accountNumber);
            
            // Send suspicious login notification
            if (failedAttempts > 0) {
                new Thread(() -> {
                    notificationService.sendSuspiciousLoginNotification(accountNumber, failedAttempts);
                }).start();
            }
            
            if (failedAttempts >= 3) {
                // Send account blocked notification
                new Thread(() -> {
                    notificationService.sendAccountBlockedNotification(accountNumber, "Multiple failed login attempts");
                }).start();
                showError("⚠️ ACCOUNT BLOCKED!\n\n" +
                         "Your account has been blocked for security reasons.\n\n" +
                         "TO UNBLOCK:\n" +
                         "• Check your email for recovery instructions\n" +
                         "• Contact: support@mybank.com\n" +
                         "• Call: 1-800-MYBANK-HELP\n" +
                         "• Visit any branch with ID\n\n" +
                         "Staff will verify your identity and unblock your account.");
            } else {
                int attemptsLeft = 3 - failedAttempts;
                showError("❌ Incorrect password!\n\n" +
                         "You have " + attemptsLeft + " attempt(s) remaining.\n" +
                         "After 3 failed attempts, your account will be blocked.\n\n" +
                         "⚠️ Warning: If you forgot your password, contact support now.");
            }
        }
    }
    
    /**
     * Shows password setup dialog
     * @param accountNumber The account number
     * @param isMandatory Whether password change is mandatory (for default password users)
     */
    private void showPasswordSetupDialog(int accountNumber, boolean isMandatory) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PasswordSetup.fxml"));
            Parent root = loader.load();
            
            PasswordSetupController controller = loader.getController();
            controller.setAccountNumber(accountNumber);
            controller.setMandatory(isMandatory);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isMandatory ? "Change Your Password (Required)" : "Set Up Password");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            
            // Prevent closing if mandatory
            if (isMandatory) {
                dialogStage.setOnCloseRequest(event -> {
                    event.consume(); // Prevent closing
                });
            }
            
            dialogStage.showAndWait();
            
            // After password setup, redirect to dashboard
            Main.changeScene("Dashboard.fxml");
            
        } catch (Exception e) {
            System.err.println("Error showing password setup dialog: " + e.getMessage());
            e.printStackTrace();
            // If dialog fails and not mandatory, still redirect to dashboard
            if (!isMandatory) {
                try {
                    Main.changeScene("Dashboard.fxml");
                } catch (Exception ex) {
                    System.err.println("Error redirecting to dashboard: " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Handles clear button click
     */
    @FXML
    private void handleClear() {
        phoneNumberField.clear();
        passwordField.clear();
        messageLabel.setText("");
        phoneNumberField.requestFocus();
    }
    
    /**
     * Opens registration page
     */
    @FXML
    private void openRegistration() {
        try {
            Main.changeScene("CreateAccount.fxml");
        } catch (Exception e) {
            System.err.println("Error opening registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles staff portal button click
     */
    @FXML
    private void handleStaffPortal() {
        Main.showStaffLogin();
    }
    
    /**
     * Handles admin portal button click
     */
    @FXML
    private void handleAdminPortal() {
        Main.showAdminLogin();
    }
    
    /**
     * Displays error message
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
    }
    
    /**
     * Displays success message
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
    }
}
