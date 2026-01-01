package com.mybank.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.models.Admin;
import com.mybank.services.AdminService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

/**
 * Controller for Transaction Monitoring.
 * This module allows admins to monitor all transactions system-wide.
 */
public class AdminTransactionMonitoringController {
    
    @FXML private TableView<TransactionRecord> transactionTable;
    @FXML private TableColumn<TransactionRecord, String> transactionIdColumn;
    @FXML private TableColumn<TransactionRecord, String> accountNumberColumn;
    @FXML private TableColumn<TransactionRecord, String> typeColumn;
    @FXML private TableColumn<TransactionRecord, String> amountColumn;
    @FXML private TableColumn<TransactionRecord, String> dateColumn;
    @FXML private TableColumn<TransactionRecord, String> descriptionColumn;
    @FXML private TableColumn<TransactionRecord, String> statusColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label messageLabel;
    @FXML private Label statsLabel;
    
    private ObservableList<TransactionRecord> transactionList = FXCollections.observableArrayList();
    private Admin currentAdmin;
    
    @FXML
    private void initialize() {
        currentAdmin = AdminService.getCurrentAdmin();
        
        // Setup table columns
        transactionIdColumn.setCellValueFactory(data -> data.getValue().transactionIdProperty());
        accountNumberColumn.setCellValueFactory(data -> data.getValue().accountNumberProperty());
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());
        amountColumn.setCellValueFactory(data -> data.getValue().amountProperty());
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        
        // Setup filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All Transactions", "Deposit", "Withdraw", "Transfer", "Flagged", "Today"
        ));
        filterComboBox.setValue("All Transactions");
        
        // Add listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTransactions());
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterTransactions());
        
        // Load transactions
        loadTransactions();
    }
    
    /**
     * Load all transactions
     */
    private void loadTransactions() {
        transactionList.clear();
        
        try {
            Connection conn = DatabaseHelper.getConnection();
            String sql = "SELECT id, accountNumber, type, amount, timestamp FROM transactions ORDER BY timestamp DESC LIMIT 1000";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                transactionList.add(new TransactionRecord(
                    rs.getInt("id"),
                    rs.getInt("accountNumber"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("timestamp"),
                    "N/A"
                ));
            }
            
            transactionTable.setItems(transactionList);
            updateStats();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load transactions: " + e.getMessage());
        }
    }
    
    /**
     * Filter transactions based on search and filter criteria
     */
    private void filterTransactions() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filter = filterComboBox.getValue();
        
        ObservableList<TransactionRecord> filtered = FXCollections.observableArrayList();
        
        for (TransactionRecord txn : transactionList) {
            // Apply filter
            if (!filter.equals("All Transactions")) {
                if (filter.equals("Today")) {
                    if (!txn.getDate().startsWith(LocalDate.now().toString())) {
                        continue;
                    }
                } else if (!txn.getType().equalsIgnoreCase(filter)) {
                    continue;
                }
            }
            
            // Apply search
            if (!searchText.isEmpty()) {
                if (!txn.getAccountNumber().contains(searchText) &&
                    !txn.getType().toLowerCase().contains(searchText) &&
                    !txn.getDescription().toLowerCase().contains(searchText)) {
                    continue;
                }
            }
            
            filtered.add(txn);
        }
        
        transactionTable.setItems(filtered);
        updateStats();
    }
    
    /**
     * Update statistics
     */
    private void updateStats() {
        int totalCount = transactionTable.getItems().size();
        double totalAmount = transactionTable.getItems().stream()
            .mapToDouble(t -> t.getAmount())
            .sum();
        
        statsLabel.setText(String.format("Showing %d transactions | Total Amount: ৳%.2f", 
            totalCount, totalAmount));
    }
    
    /**
     * Handle search with date range
     */
    @FXML
    private void handleSearch() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            showError("Please select both start and end dates");
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            showError("Start date must be before end date");
            return;
        }
        
        transactionList.clear();
        
        try {
            Connection conn = DatabaseHelper.getConnection();
            String sql = "SELECT id, accountNumber, type, amount, timestamp FROM transactions " +
                        "WHERE DATE(timestamp) BETWEEN ? AND ? " +
                        "ORDER BY timestamp DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactionList.add(new TransactionRecord(
                    rs.getInt("id"),
                    rs.getInt("accountNumber"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("timestamp"),
                    "N/A"
                ));
            }
            
            transactionTable.setItems(transactionList);
            updateStats();
            showSuccess("Found " + transactionList.size() + " transactions in date range");
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to search transactions: " + e.getMessage());
        }
    }
    
    /**
     * Handle flag transaction
     */
    @FXML
    private void handleFlagTransaction() {
        TransactionRecord selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a transaction to flag");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Flag Transaction");
        dialog.setHeaderText("Flag transaction #" + selected.getTransactionId());
        dialog.setContentText("Reason:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String reason = result.get().trim();
            
            AdminService.logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(),
                "FLAG", "TRANSACTION", "Flagged transaction #" + selected.getTransactionId() + ": " + reason, "SUCCESS");
            
            showSuccess("Transaction flagged successfully. Alert logged for review.");
        }
    }
    
    /**
     * Handle reverse transaction (with confirmation)
     */
    @FXML
    private void handleReverseTransaction() {
        TransactionRecord selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a transaction to reverse");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reverse Transaction");
        confirmAlert.setHeaderText("Confirm Transaction Reversal");
        confirmAlert.setContentText("Are you sure you want to reverse this transaction?\n\n" +
            "Transaction ID: " + selected.getTransactionId() + "\n" +
            "Account: " + selected.getAccountNumber() + "\n" +
            "Type: " + selected.getType() + "\n" +
            "Amount: ৳" + selected.getAmount() + "\n\n" +
            "This action cannot be undone!");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // For demonstration - log the reversal action
                AdminService.logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(),
                    "REVERSE", "TRANSACTION", "Reversed transaction #" + selected.getTransactionId() + 
                    " for account " + selected.getAccountNumber(), "SUCCESS");
                
                showSuccess("Transaction reversal logged. Contact technical team to complete the process.");
                
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to reverse transaction: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle export transactions
     */
    @FXML
    private void handleExport() {
        showSuccess("Export feature: " + transactionTable.getItems().size() + " transactions ready. " +
            "Implement CSV/PDF export as needed.");
    }
    
    /**
     * Handle refresh
     */
    @FXML
    private void handleRefresh() {
        loadTransactions();
        searchField.clear();
        filterComboBox.setValue("All Transactions");
        if (startDatePicker != null) startDatePicker.setValue(null);
        if (endDatePicker != null) endDatePicker.setValue(null);
        showSuccess("Transactions refreshed");
    }
    
    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
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
     * Show error message
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    /**
     * Transaction record class for TableView
     */
    public static class TransactionRecord {
        private final SimpleStringProperty transactionId;
        private final SimpleStringProperty accountNumber;
        private final SimpleStringProperty type;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty date;
        private final SimpleStringProperty description;
        private final SimpleStringProperty status;
        
        public TransactionRecord(int transactionId, int accountNumber, String type, 
                                double amount, String date, String description) {
            this.transactionId = new SimpleStringProperty(String.valueOf(transactionId));
            this.accountNumber = new SimpleStringProperty(String.valueOf(accountNumber));
            this.type = new SimpleStringProperty(type);
            this.amount = new SimpleStringProperty(String.format("৳%.2f", amount));
            this.date = new SimpleStringProperty(date);
            this.description = new SimpleStringProperty(description);
            this.status = new SimpleStringProperty("Normal");
        }
        
        public SimpleStringProperty transactionIdProperty() { return transactionId; }
        public SimpleStringProperty accountNumberProperty() { return accountNumber; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleStringProperty statusProperty() { return status; }
        
        public String getTransactionId() { return transactionId.get(); }
        public String getAccountNumber() { return accountNumber.get(); }
        public String getType() { return type.get(); }
        public double getAmount() { 
            return Double.parseDouble(amount.get().replace("৳", "").replace(",", "")); 
        }
        public String getDate() { return date.get(); }
        public String getDescription() { return description.get(); }
    }
}
