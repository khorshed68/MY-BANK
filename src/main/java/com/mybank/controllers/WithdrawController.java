package com.mybank.controllers;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.services.NotificationService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Withdraw Controller
 * Handles withdrawal functionality
 */
public class WithdrawController {
    
    @FXML
    private TextField accountNumberField;
    
    @FXML
    private TextField withdrawAmountField;
    
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
     * Withdraws money from account
     */
    @FXML
    private void withdrawMoney() {
        // Clear previous message
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: black;");
        
        try {
            // Validate inputs
            if (accountNumberField.getText().trim().isEmpty()) {
                showError("Please enter account number!");
                return;
            }
            
            if (withdrawAmountField.getText().trim().isEmpty()) {
                showError("Please enter withdrawal amount!");
                return;
            }
            
            // Parse values
            int accountNumber = Integer.parseInt(accountNumberField.getText().trim());
            double amount = Double.parseDouble(withdrawAmountField.getText().trim());
            
            // Validate values
            if (accountNumber <= 0) {
                showError("Account number must be positive!");
                return;
            }
            
            if (amount <= 0) {
                showError("Withdrawal amount must be greater than zero!");
                return;
            }
            
            // Check if account exists
            if (!dbHelper.accountExists(accountNumber)) {
                showError("Account not found!");
                return;
            }
            
            // Check balance
            double currentBalance = dbHelper.getBalance(accountNumber);
            if (currentBalance < amount) {
                showError("Insufficient balance!\nCurrent Balance: " + String.format("%.2f", currentBalance) + " TAKA");
                return;
            }
            
            // Perform withdrawal
            boolean success = dbHelper.withdraw(accountNumber, amount);
            
            if (success) {
                double newBalance = dbHelper.getBalance(accountNumber);
                
                // Send withdrawal notification
                final int accNum = accountNumber;
                final double amt = amount;
                final double balance = newBalance;
                new Thread(() -> {
                    notificationService.sendWithdrawalNotification(accNum, amt, balance);
                }).start();
                
                showSuccess("Withdrawal successful!\nAmount Withdrawn: " + String.format("%.2f", amount) + " TAKA" + 
                           "\nNew Balance: " + String.format("%.2f", newBalance) + " TAKA");
                clearFields();
            } else {
                showError("Withdrawal failed. Please try again.");
            }
            
        } catch (NumberFormatException e) {
            showError("Invalid input! Please enter valid numbers.");
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
    
    /**
     * Clears all input fields
     */
    private void clearFields() {
        accountNumberField.clear();
        withdrawAmountField.clear();
    }
}
