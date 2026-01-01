package com.mybank.controllers;

import com.mybank.Main;
import com.mybank.services.AdminService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for Admin Login screen.
 */
public class AdminLoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label messageLabel;
    
    /**
     * Handle login
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (username.isEmpty()) {
            showError("Please enter username");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter password");
            passwordField.requestFocus();
            return;
        }
        
        // Authenticate
        boolean success = AdminService.authenticate(username, password);
        
        if (success) {
            showSuccess("Login successful!");
            
            // Clear fields
            clearFields();
            
            // Navigate to admin dashboard
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    javafx.application.Platform.runLater(() -> {
                        Main.showAdminDashboard();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } else {
            showError("Invalid username or password. Please try again.");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }
    
    /**
     * Handle back to main menu
     */
    @FXML
    private void handleBack() {
        Main.showLogin();
    }
    
    /**
     * Handle register navigation
     */
    @FXML
    private void handleRegister() {
        Main.showAdminRegistration();
    }
    
    /**
     * Clear input fields
     */
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    /**
     * Hide message
     */
    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
    
    /**
     * Initialize controller
     */
    @FXML
    private void initialize() {
        hideMessage();
        
        // Initialize admin service
        AdminService.initialize();
        
        // Add listeners to clear error on input
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        
        // Enter key to login
        passwordField.setOnAction(event -> handleLogin());
    }
}
