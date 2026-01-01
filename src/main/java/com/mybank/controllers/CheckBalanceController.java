package com.mybank.controllers;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Check Balance Controller
 * Handles balance inquiry functionality
 */
public class CheckBalanceController {
    
    @FXML
    private TextField accountNumberField;
    
    @FXML
    private Label ownerNameLabel;
    
    @FXML
    private Label balanceLabel;
    
    @FXML
    private Label messageLabel;
    
    private DatabaseHelper dbHelper;
    
    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        dbHelper = new DatabaseHelper();
        ownerNameLabel.setText("");
        balanceLabel.setText("");
    }
    
    /**
     * Checks account balance
     */
    @FXML
    private void checkBalance() {
        // Clear previous messages
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: black;");
        ownerNameLabel.setText("");
        balanceLabel.setText("");
        
        try {
            // Validate input
            if (accountNumberField.getText().trim().isEmpty()) {
                showError("Please enter account number!");
                return;
            }
            
            // Parse value
            int accountNumber = Integer.parseInt(accountNumberField.getText().trim());
            
            // Validate value
            if (accountNumber <= 0) {
                showError("Account number must be positive!");
                return;
            }
            
            // Check if account exists
            if (!dbHelper.accountExists(accountNumber)) {
                showError("Account not found!");
                return;
            }
            
            // Get account details
            String ownerName = dbHelper.getOwnerName(accountNumber);
            double balance = dbHelper.getBalance(accountNumber);
            
            // Display information
            ownerNameLabel.setText("Account Holder: " + ownerName);
            balanceLabel.setText("Current Balance: " + String.format("%.2f", balance) + " TAKA");
            
            ownerNameLabel.setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
            balanceLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            
        } catch (NumberFormatException e) {
            showError("Invalid input! Please enter a valid account number.");
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
}
