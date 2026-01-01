package com.mybank.controllers;

import com.mybank.Main;
import com.mybank.models.Staff;
import com.mybank.services.StaffService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for staff login functionality.
 * Handles staff authentication and navigation to staff dashboard.
 */
public class StaffLoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button loginButton;
    
    /**
     * Handle staff login
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
        
        // Authenticate staff
        Staff staff = StaffService.authenticateStaff(username, password);
        
        if (staff != null) {
            System.out.println("Staff login successful: " + staff.getFullName() + 
                             " (" + staff.getRoleDisplay() + ")");
            
            // Store staff session
            Main.setCurrentStaff(staff);
            
            // Navigate to staff dashboard
            Main.showStaffDashboard();
            
        } else {
            showError("Invalid username or password. Please try again.");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }
    
    /**
     * Handle back to main login
     */
    @FXML
    private void handleBack() {
        Main.showLogin();
    }
    
    /**
     * Handle registration
     */
    @FXML
    private void handleRegister() {
        Main.showStaffRegistration();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    /**
     * Hide error message
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    /**
     * Initialize controller
     */
    @FXML
    private void initialize() {
        hideError();
        
        // Add listeners to clear error on input
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> hideError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> hideError());
        
        // Focus on username field
        usernameField.requestFocus();
    }
}
