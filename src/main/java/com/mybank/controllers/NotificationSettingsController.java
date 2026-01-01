package com.mybank.controllers;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Notification Settings Controller
 * Handles notification preference management
 */
public class NotificationSettingsController {
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneNumberField;
    
    @FXML
    private CheckBox emailNotificationsCheckBox;
    
    @FXML
    private CheckBox smsNotificationsCheckBox;
    
    @FXML
    private Label currentEmailLabel;
    
    @FXML
    private Label currentPhoneLabel;
    
    @FXML
    private Label messageLabel;
    
    private DatabaseHelper dbHelper;
    private int loggedInAccount;
    
    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        dbHelper = new DatabaseHelper();
        loggedInAccount = Main.getLoggedInAccount();
        loadCurrentSettings();
    }
    
    /**
     * Loads current notification settings
     */
    private void loadCurrentSettings() {
        if (loggedInAccount <= 0) {
            showError("Please login first!");
            return;
        }
        
        String[] contactInfo = dbHelper.getAccountContactInfo(loggedInAccount);
        if (contactInfo != null) {
            String email = contactInfo[0];
            String phone = contactInfo[1];
            boolean smsEnabled = "1".equals(contactInfo[2]);
            boolean emailEnabled = "1".equals(contactInfo[3]);
            
            // Display current settings
            currentEmailLabel.setText(email != null && !email.isEmpty() ? email : "Not set");
            currentPhoneLabel.setText(phone != null && !phone.isEmpty() ? phone : "Not set");
            
            // Set checkboxes
            emailNotificationsCheckBox.setSelected(emailEnabled);
            smsNotificationsCheckBox.setSelected(smsEnabled);
            
            // Pre-fill fields
            if (email != null) emailField.setText(email);
            if (phone != null) phoneNumberField.setText(phone);
        }
    }
    
    /**
     * Saves notification settings
     */
    @FXML
    private void saveSettings() {
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: black;");
        
        try {
            if (loggedInAccount <= 0) {
                showError("Please login first!");
                return;
            }
            
            String email = emailField.getText().trim();
            String phone = phoneNumberField.getText().trim();
            boolean emailEnabled = emailNotificationsCheckBox.isSelected();
            boolean smsEnabled = smsNotificationsCheckBox.isSelected();
            
            // Validate email format if provided
            if (!email.isEmpty() && !email.contains("@")) {
                showError("Please enter a valid email address!");
                return;
            }
            
            // Validate phone format if provided
            if (!phone.isEmpty() && phone.length() < 10) {
                showError("Please enter a valid phone number (at least 10 digits)!");
                return;
            }
            
            // Update contact info
            boolean contactUpdated = dbHelper.updateContactInfo(loggedInAccount, email, phone);
            
            // Update notification preferences
            boolean prefsUpdated = dbHelper.updateNotificationPreferences(loggedInAccount, smsEnabled, emailEnabled);
            
            if (contactUpdated && prefsUpdated) {
                showSuccess("Notification settings saved successfully!\n" +
                           "Email Notifications: " + (emailEnabled ? "Enabled" : "Disabled") + "\n" +
                           "SMS Notifications: " + (smsEnabled ? "Enabled" : "Disabled"));
                loadCurrentSettings(); // Refresh display
            } else {
                showError("Failed to save settings. Please try again.");
            }
            
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Returns to dashboard
     */
    @FXML
    private void backToDashboard() {
        try {
            Main.changeScene("Dashboard.fxml");
        } catch (Exception e) {
            System.err.println("Error returning to dashboard: " + e.getMessage());
            e.printStackTrace();
        }
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
