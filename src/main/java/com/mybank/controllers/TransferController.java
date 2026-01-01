package com.mybank.controllers;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.services.NotificationService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Transfer Controller
 * Handles fund transfer functionality
 */
public class TransferController {
    
    @FXML
    private TextField fromAccountField;
    
    @FXML
    private TextField toAccountField;
    
    @FXML
    private TextField transferAmountField;
    
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
     * Transfers money between accounts
     */
    @FXML
    private void transferMoney() {
        // Clear previous message
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: black;");
        
        try {
            // Validate inputs
            if (fromAccountField.getText().trim().isEmpty()) {
                showError("Please enter sender's account number!");
                return;
            }
            
            if (toAccountField.getText().trim().isEmpty()) {
                showError("Please enter receiver's account number!");
                return;
            }
            
            if (transferAmountField.getText().trim().isEmpty()) {
                showError("Please enter transfer amount!");
                return;
            }
            
            // Parse values
            int fromAccount = Integer.parseInt(fromAccountField.getText().trim());
            int toAccount = Integer.parseInt(toAccountField.getText().trim());
            double amount = Double.parseDouble(transferAmountField.getText().trim());
            
            // Validate values
            if (fromAccount <= 0 || toAccount <= 0) {
                showError("Account numbers must be positive!");
                return;
            }
            
            if (fromAccount == toAccount) {
                showError("Cannot transfer to the same account!");
                return;
            }
            
            if (amount <= 0) {
                showError("Transfer amount must be greater than zero!");
                return;
            }
            
            // Check if accounts exist
            if (!dbHelper.accountExists(fromAccount)) {
                showError("Sender's account not found!");
                return;
            }
            
            if (!dbHelper.accountExists(toAccount)) {
                showError("Receiver's account not found!");
                return;
            }
            
            // Check balance
            double senderBalance = dbHelper.getBalance(fromAccount);
            if (senderBalance < amount) {
                showError("Insufficient balance in sender's account!\nCurrent Balance: " + 
                          String.format("%.2f", senderBalance) + " TAKA");
                return;
            }
            
            // Perform transfer
            boolean success = dbHelper.transfer(fromAccount, toAccount, amount);
            
            if (success) {
                double newSenderBalance = dbHelper.getBalance(fromAccount);
                double newReceiverBalance = dbHelper.getBalance(toAccount);
                
                // Send transfer notifications to both sender and receiver
                final int from = fromAccount;
                final int to = toAccount;
                final double amt = amount;
                final double senderBal = newSenderBalance;
                final double receiverBal = newReceiverBalance;
                new Thread(() -> {
                    notificationService.sendTransferNotifications(from, to, amt, senderBal, receiverBal);
                }).start();
                
                String senderName = dbHelper.getOwnerName(fromAccount);
                String receiverName = dbHelper.getOwnerName(toAccount);
                
                showSuccess("Transfer successful!\nAmount: TAKA " + String.format("%.2f", amount) + 
                           "\nFrom: " + senderName + " (A/C: " + fromAccount + ")" +
                           "\nTo: " + receiverName + " (A/C: " + toAccount + ")" +
                           "\n\nSender's New Balance: TAKA " + String.format("%.2f", newSenderBalance));
                clearFields();
            } else {
                showError("Transfer failed. Please try again.");
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
        fromAccountField.clear();
        toAccountField.clear();
        transferAmountField.clear();
    }
}
