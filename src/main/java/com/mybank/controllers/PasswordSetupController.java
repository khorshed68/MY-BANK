package com.mybank.controllers;

import com.mybank.database.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

/**
 * Password Setup Controller
 * Handles password creation for first-time users
 */
public class PasswordSetupController {
    
    @FXML
    private Label accountInfoLabel;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label messageLabel;
    
    private int accountNumber;
    private DatabaseHelper dbHelper;
    private boolean isMandatory = false;
    
    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        dbHelper = new DatabaseHelper();
    }
    
    /**
     * Sets the account number for password setup
     * @param accountNumber The account number
     */
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
        updateAccountInfo();
    }
    
    /**
     * Sets whether password setup is mandatory (cannot be skipped)
     * @param isMandatory true if mandatory, false if optional
     */
    public void setMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
        if (isMandatory) {
            messageLabel.setText("You must change your default password before continuing.");
            messageLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
        }
    }
    
    /**
     * Updates account information display
     */
    private void updateAccountInfo() {
        try {
            String ownerName = dbHelper.getAccountOwnerName(accountNumber);
            accountInfoLabel.setText("Account: " + accountNumber + " - " + ownerName);
        } catch (Exception e) {
            accountInfoLabel.setText("Account: " + accountNumber);
        }
    }
    
    /**
     * Handles create password button click
     */
    @FXML
    private void handleCreatePassword() {
        // Clear previous message
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: black;");
        
        try {
            // Validate inputs
            if (newPasswordField.getText().trim().isEmpty()) {
                showError("Please enter a password!");
                return;
            }
            
            if (confirmPasswordField.getText().trim().isEmpty()) {
                showError("Please confirm your password!");
                return;
            }
            
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            // Validate password length
            if (newPassword.length() < 6) {
                showError("Password must be at least 6 characters long!");
                return;
            }
            
            // Check if passwords match
            if (!newPassword.equals(confirmPassword)) {
                showError("Passwords do not match!");
                return;
            }
            
            // Update password in database
            boolean success = dbHelper.updatePassword(accountNumber, newPassword);
            
            if (success) {
                showSuccess("Password created successfully!");
                
                // Close dialog after a short delay
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(() -> {
                            closeDialog();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                
            } else {
                showError("Failed to create password. Please try again.");
            }
            
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles skip button click
     */
    @FXML
    private void handleSkip() {
        if (isMandatory) {
            showError("You must change your default password to continue!");
            return;
        }
        // User can skip password setup and set it up later
        closeDialog();
    }
    
    /**
     * Closes the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) newPasswordField.getScene().getWindow();
        stage.close();
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
