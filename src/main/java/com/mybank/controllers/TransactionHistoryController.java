package com.mybank.controllers;

import java.sql.ResultSet;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.models.Transaction;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Transaction History Controller
 * Handles transaction history display
 */
public class TransactionHistoryController {
    
    @FXML
    private TextField accountNumberField;
    
    @FXML
    private Label accountInfoLabel;
    
    @FXML
    private TableView<Transaction> transactionTable;
    
    @FXML
    private TableColumn<Transaction, Integer> idColumn;
    
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    
    @FXML
    private TableColumn<Transaction, String> typeColumn;
    
    @FXML
    private TableColumn<Transaction, Double> amountColumn;
    
    @FXML
    private TableColumn<Transaction, Integer> accountColumn;
    
    @FXML
    private Label messageLabel;
    
    private DatabaseHelper dbHelper;
    private ObservableList<Transaction> transactionList;
    
    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        dbHelper = new DatabaseHelper();
        transactionList = FXCollections.observableArrayList();
        
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        accountColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        
        // Format amount column to show currency
        amountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f TAKA", amount));
                }
            }
        });
        
        transactionTable.setItems(transactionList);
        accountInfoLabel.setText("");
    }
    
    /**
     * Views transaction history for an account
     */
    @FXML
    private void viewHistory() {
        // Clear previous data
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: black;");
        accountInfoLabel.setText("");
        transactionList.clear();
        
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
            
            // Get account info
            String ownerName = dbHelper.getOwnerName(accountNumber);
            double balance = dbHelper.getBalance(accountNumber);
            
            accountInfoLabel.setText("Account: " + accountNumber + " | Owner: " + ownerName + 
                                    " | Current Balance: " + String.format("%.2f", balance) + " TAKA");
            accountInfoLabel.setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
            
            // Get transaction history
            ResultSet rs = dbHelper.getTransactionHistory(accountNumber);
            
            if (rs != null) {
                int count = 0;
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getInt("accountNumber"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("timestamp")
                    );
                    transactionList.add(transaction);
                    count++;
                }
                
                if (count == 0) {
                    showInfo("No transactions found for this account.");
                } else {
                    showInfo(count + " transaction(s) found.");
                }
            } else {
                showError("Error retrieving transaction history.");
            }
            
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
    
    /**
     * Displays info message
     */
    private void showInfo(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
    }
}
