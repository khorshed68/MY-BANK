package com.mybank.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.models.Account;
import com.mybank.models.Staff;
import com.mybank.models.Transaction;
import com.mybank.services.NotificationService;
import com.mybank.services.StaffService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for customer management.
 * Allows staff to search, view, and manage customer accounts.
 */
public class CustomerManagementController {
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private TableView<Account> customersTable;
    
    @FXML
    private TableColumn<Account, String> accountNumberColumn;
    
    @FXML
    private TableColumn<Account, String> customerNameColumn;
    
    @FXML
    private TableColumn<Account, String> emailColumn;
    
    @FXML
    private TableColumn<Account, String> phoneColumn;
    
    @FXML
    private TableColumn<Account, String> accountTypeColumn;
    
    @FXML
    private TableColumn<Account, Double> balanceColumn;
    
    @FXML
    private TableColumn<Account, String> statusColumn;
    
    @FXML
    private TableColumn<Account, String> createdDateColumn;
    
    @FXML
    private VBox detailsPanel;
    
    @FXML
    private Label detailAccountNumber;
    
    @FXML
    private Label detailCustomerName;
    
    @FXML
    private Label detailEmail;
    
    @FXML
    private Label detailPhone;
    
    @FXML
    private Label detailAccountType;
    
    @FXML
    private Label detailBalance;
    
    @FXML
    private Label detailStatus;
    
    @FXML
    private Label detailCreatedDate;
    
    @FXML
    private Button suspendBtn;
    
    @FXML
    private Button activateBtn;
    
    @FXML
    private Button unblockBtn;
    
    @FXML
    private Button deleteBtn;
    
    private Staff currentStaff;
    private Account selectedAccount;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        currentStaff = Main.getCurrentStaff();
        
        // Populate status filter
        statusFilter.getItems().addAll("All", "ACTIVE", "SUSPENDED", "BLOCKED", "CLOSED");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> loadCustomers());
        
        setupTable();
        loadCustomers();
    }
    
    /**
     * Setup table columns
     */
    private void setupTable() {
        accountNumberColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumberString"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        accountTypeColumn.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        
        // Format balance column
        balanceColumn.setCellFactory(column -> new TableCell<Account, Double>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                if (empty || balance == null) {
                    setText(null);
                } else {
                    setText(String.format("৳%.2f", balance));
                }
            }
        });
        
        // Status column with color coding
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("accountStatus"));
        statusColumn.setCellFactory(column -> new TableCell<Account, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "ACTIVE":
                            setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                            break;
                        case "SUSPENDED":
                            setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                            break;
                        case "BLOCKED":
                            setStyle("-fx-text-fill: #9c27b0; -fx-font-weight: bold;");
                            break;
                        case "CLOSED":
                            setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        
        // Add row click listener
        customersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && customersTable.getSelectionModel().getSelectedItem() != null) {
                showCustomerDetails();
            }
        });
    }
    
    /**
     * Load customers from database
     */
    private void loadCustomers() {
        String searchText = searchField.getText().trim();
        String status = statusFilter.getValue();
        
        List<Account> accounts = new ArrayList<>();
        
        String sql = "SELECT * FROM accounts WHERE 1=1";
        
        if (!searchText.isEmpty()) {
            sql += " AND (ownerName LIKE ? OR CAST(accountNumber AS TEXT) LIKE ? OR phoneNumber LIKE ? OR email LIKE ?)";
        }
        
        if (status != null && !status.equals("All")) {
            sql += " AND status = ?";
        }
        
        sql += " ORDER BY accountNumber DESC";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            
            if (!searchText.isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            if (status != null && !status.equals("All")) {
                pstmt.setString(paramIndex++, status);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Account account = new Account();
                account.setAccountNumber(rs.getInt("accountNumber"));
                account.setCustomerName(rs.getString("ownerName"));
                account.setEmail(rs.getString("email"));
                account.setPhoneNumber(rs.getString("phoneNumber"));
                account.setAccountType(rs.getString("accountType"));
                account.setBalance(rs.getDouble("balance"));
                account.setAccountStatus(rs.getString("status"));
                // Handle both old and new column names
                try {
                    account.setCreatedDate(rs.getString("createdDate"));
                } catch (Exception e) {
                    try {
                        account.setCreatedDate(rs.getString("created_date"));
                    } catch (Exception ex) {
                        account.setCreatedDate("");
                    }
                }
                accounts.add(account);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading customers: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading customers: " + e.getMessage());
        }
        
        ObservableList<Account> accountList = FXCollections.observableArrayList(accounts);
        customersTable.setItems(accountList);
    }
    
    /**
     * Show customer details panel
     */
    private void showCustomerDetails() {
        selectedAccount = customersTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) return;
        
        detailAccountNumber.setText(String.valueOf(selectedAccount.getAccountNumber()));
        detailCustomerName.setText(selectedAccount.getCustomerName());
        detailEmail.setText(selectedAccount.getEmail());
        detailPhone.setText(selectedAccount.getPhoneNumber());
        detailAccountType.setText(selectedAccount.getAccountType());
        detailBalance.setText(String.format("৳%.2f", selectedAccount.getBalance()));
        detailStatus.setText(selectedAccount.getAccountStatus());
        detailCreatedDate.setText(selectedAccount.getCreatedDate());
        
        // Show appropriate action buttons
        if ("ACTIVE".equals(selectedAccount.getAccountStatus())) {
            suspendBtn.setVisible(true);
            suspendBtn.setManaged(true);
            activateBtn.setVisible(false);
            activateBtn.setManaged(false);
            unblockBtn.setVisible(false);
            unblockBtn.setManaged(false);
        } else if ("SUSPENDED".equals(selectedAccount.getAccountStatus())) {
            suspendBtn.setVisible(false);
            suspendBtn.setManaged(false);
            activateBtn.setVisible(true);
            activateBtn.setManaged(true);
            unblockBtn.setVisible(false);
            unblockBtn.setManaged(false);
        } else if ("BLOCKED".equals(selectedAccount.getAccountStatus())) {
            suspendBtn.setVisible(false);
            suspendBtn.setManaged(false);
            activateBtn.setVisible(false);
            activateBtn.setManaged(false);
            unblockBtn.setVisible(true);
            unblockBtn.setManaged(true);
        } else {
            suspendBtn.setVisible(false);
            suspendBtn.setManaged(false);
            activateBtn.setVisible(false);
            activateBtn.setManaged(false);
            unblockBtn.setVisible(false);
            unblockBtn.setManaged(false);
        }
        
        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);
    }
    
    /**
     * Handle search button
     */
    @FXML
    private void handleSearch() {
        loadCustomers();
    }
    
    /**
     * Handle refresh button
     */
    @FXML
    private void handleRefresh() {
        searchField.clear();
        statusFilter.setValue("All");
        loadCustomers();
    }
    
    /**
     * Handle close details panel
     */
    @FXML
    private void handleCloseDetails() {
        detailsPanel.setVisible(false);
        detailsPanel.setManaged(false);
        customersTable.getSelectionModel().clearSelection();
    }
    
    /**
     * Handle suspend account
     */
    @FXML
    private void handleSuspendAccount() {
        if (selectedAccount == null) return;
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Suspend Account");
        confirmAlert.setHeaderText("Suspend Account: " + selectedAccount.getAccountNumber());
        confirmAlert.setContentText("Are you sure you want to suspend this account?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (updateAccountStatus(String.valueOf(selectedAccount.getAccountNumber()), "SUSPENDED")) {
                    StaffService.logActivity(currentStaff.getStaffId(), "SUSPEND_ACCOUNT", 
                                            selectedAccount.getAccountNumber(), 
                                            "Account suspended");
                    showSuccess("Account suspended successfully");
                    loadCustomers();
                    handleCloseDetails();
                }
            }
        });
    }
    
    /**
     * Handle activate account
     */
    @FXML
    private void handleActivateAccount() {
        if (selectedAccount == null) return;
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Activate Account");
        confirmAlert.setHeaderText("Activate Account: " + selectedAccount.getAccountNumber());
        confirmAlert.setContentText("Are you sure you want to activate this account?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (updateAccountStatus(String.valueOf(selectedAccount.getAccountNumber()), "ACTIVE")) {
                    StaffService.logActivity(currentStaff.getStaffId(), "ACTIVATE_ACCOUNT", 
                                            selectedAccount.getAccountNumber(), 
                                            "Account activated");
                    showSuccess("Account activated successfully");
                    loadCustomers();
                    handleCloseDetails();
                }
            }
        });
    }
    
    /**
     * Handle unblock account
     */
    @FXML
    private void handleUnblockAccount() {
        if (selectedAccount == null) return;
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Unblock Account");
        confirmAlert.setHeaderText("Unblock Account: " + selectedAccount.getAccountNumber());
        confirmAlert.setContentText("This account was blocked due to failed login attempts.\n\n" +
                                    "Customer: " + selectedAccount.getCustomerName() + "\n" +
                                    "Phone: " + selectedAccount.getPhoneNumber() + "\n" +
                                    "Email: " + selectedAccount.getEmail() + "\n\n" +
                                    "Are you sure you want to unblock this account?\n" +
                                    "This will reset failed login attempts and reactivate the account.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (unblockAccount(selectedAccount.getAccountNumber())) {
                    // Send reactivation notification email
                    try {
                        NotificationService notificationService = new NotificationService();
                        notificationService.sendAccountReactivatedNotification(selectedAccount.getAccountNumber());
                    } catch (Exception e) {
                        System.err.println("Failed to send reactivation notification: " + e.getMessage());
                    }
                    
                    StaffService.logActivity(currentStaff.getStaffId(), "UNBLOCK_ACCOUNT", 
                                            selectedAccount.getAccountNumber(), 
                                            "Account unblocked by staff: " + currentStaff.getFullName());
                    showSuccess("Account unblocked successfully!\n\n" +
                               "Customer: " + selectedAccount.getCustomerName() + "\n" +
                               "A reactivation notification has been sent to their email.");
                    loadCustomers();
                    handleCloseDetails();
                }
            }
        });
    }
    
    /**
     * Handle show blocked accounts
     */
    @FXML
    private void handleShowBlocked() {
        statusFilter.setValue("BLOCKED");
        loadCustomers();
    }
    
    /**
     * Update account status
     */
    private boolean updateAccountStatus(String accountNumber, String newStatus) {
        String sql = "UPDATE accounts SET status = ? WHERE accountNumber = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, Integer.parseInt(accountNumber));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.err.println("Error updating account status: " + e.getMessage());
            e.printStackTrace();
            showError("Error updating account status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Unblock account - reset failed attempts and change status to ACTIVE
     */
    private boolean unblockAccount(int accountNumber) {
        String sql = "UPDATE accounts SET status = ?, failedAttempts = 0 WHERE accountNumber = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "ACTIVE");
            pstmt.setInt(2, accountNumber);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.err.println("Error unblocking account: " + e.getMessage());
            e.printStackTrace();
            showError("Error unblocking account: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Handle view transactions
     */
    @FXML
    private void handleViewTransactions() {
        if (selectedAccount == null) {
            showError("Please select a customer account first.");
            return;
        }
        
        showTransactionHistoryDialog();
    }
    
    /**
     * Show transaction history dialog
     */
    private void showTransactionHistoryDialog() {
        // Create a new stage for transaction history
        Stage transactionStage = new Stage();
        transactionStage.initModality(Modality.APPLICATION_MODAL);
        transactionStage.setTitle("Transaction History - Account #" + selectedAccount.getAccountNumber());
        
        // Create table for transactions
        TableView<Transaction> transactionTable = new TableView<>();
        transactionTable.setStyle("-fx-background-color: white;");
        
        // Create columns
        TableColumn<Transaction, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(60);
        idColumn.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Date & Time");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(180);
        
        TableColumn<Transaction, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setPrefWidth(120);
        typeColumn.setStyle("-fx-alignment: CENTER;");
        
        // Style type column with colors
        typeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    switch (type.toUpperCase()) {
                        case "DEPOSIT":
                        case "TRANSFER IN":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "WITHDRAW":
                        case "TRANSFER OUT":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        TableColumn<Transaction, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setPrefWidth(150);
        amountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // Format amount column
        amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("৳%.2f", amount));
                    // Color based on transaction type
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    if (transaction != null) {
                        String type = transaction.getType().toUpperCase();
                        if (type.contains("DEPOSIT") || type.contains("TRANSFER IN")) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                        } else if (type.contains("WITHDRAW") || type.contains("TRANSFER OUT")) {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                        } else {
                            setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                        }
                    }
                }
            }
        });
        
        // Add columns to table
        transactionTable.getColumns().addAll(idColumn, dateColumn, typeColumn, amountColumn);
        
        // Load transactions from database
        List<Transaction> transactions = getTransactionsForAccount(selectedAccount.getAccountNumber());
        ObservableList<Transaction> transactionList = FXCollections.observableArrayList(transactions);
        transactionTable.setItems(transactionList);
        
        // Create header label
        Label headerLabel = new Label("Transaction History for " + selectedAccount.getCustomerName());
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10;");
        
        Label accountInfoLabel = new Label(
            "Account: " + selectedAccount.getAccountNumber() + 
            " | Type: " + selectedAccount.getAccountType() + 
            " | Balance: ৳" + String.format("%.2f", selectedAccount.getBalance())
        );
        accountInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e; -fx-padding: 5 10 10 10;");
        
        Label statsLabel = new Label("Total Transactions: " + transactions.size());
        statsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 0 10 10 10;");
        
        // Create close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        closeButton.setOnAction(e -> transactionStage.close());
        
        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        refreshButton.setOnAction(e -> {
            List<Transaction> refreshedTransactions = getTransactionsForAccount(selectedAccount.getAccountNumber());
            transactionTable.setItems(FXCollections.observableArrayList(refreshedTransactions));
            statsLabel.setText("Total Transactions: " + refreshedTransactions.size());
        });
        
        // Create button container
        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        buttonBox.setStyle("-fx-alignment: center; -fx-padding: 10;");
        buttonBox.getChildren().addAll(refreshButton, closeButton);
        
        // Create layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #ecf0f1;");
        layout.getChildren().addAll(headerLabel, accountInfoLabel, statsLabel, transactionTable, buttonBox);
        
        // Set placeholder for empty table
        Label placeholderLabel = new Label("No transactions found for this account");
        placeholderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");
        transactionTable.setPlaceholder(placeholderLabel);
        
        // Create scene and show
        Scene scene = new Scene(layout, 700, 500);
        transactionStage.setScene(scene);
        transactionStage.setResizable(true);
        transactionStage.showAndWait();
    }
    
    /**
     * Get transactions for a specific account
     */
    private List<Transaction> getTransactionsForAccount(int accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper();
        
        try {
            ResultSet rs = dbHelper.getTransactionHistory(accountNumber);
            
            if (rs != null) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getInt("accountNumber"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("timestamp")
                    );
                    transactions.add(transaction);
                }
                rs.close();
            }
        } catch (Exception e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    /**
     * Handle delete customer
     */
    @FXML
    private void handleDeleteCustomer() {
        if (selectedAccount == null) {
            showError("Please select a customer account first.");
            return;
        }
        
        // Create confirmation dialog with detailed warning
        Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
        confirmAlert.setTitle("⚠️ Delete Customer Account");
        confirmAlert.setHeaderText("CRITICAL ACTION: Permanently Delete Customer Data");
        confirmAlert.setContentText(
            "You are about to PERMANENTLY DELETE the following customer:\n\n" +
            "Account Number: " + selectedAccount.getAccountNumber() + "\n" +
            "Customer Name: " + selectedAccount.getCustomerName() + "\n" +
            "Email: " + selectedAccount.getEmail() + "\n" +
            "Phone: " + selectedAccount.getPhoneNumber() + "\n" +
            "Current Balance: ৳" + String.format("%.2f", selectedAccount.getBalance()) + "\n\n" +
            "⚠️ WARNING: This action will:\n" +
            "• Delete the customer account permanently\n" +
            "• Delete ALL transaction history\n" +
            "• Delete ALL related data from the database\n" +
            "• This action CANNOT be undone!\n\n" +
            "Are you absolutely sure you want to proceed?"
        );
        
        // Add custom buttons
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Second confirmation for critical action
                Alert secondConfirm = new Alert(Alert.AlertType.CONFIRMATION);
                secondConfirm.setTitle("Final Confirmation");
                secondConfirm.setHeaderText("Last Chance to Cancel");
                secondConfirm.setContentText(
                    "This is your FINAL confirmation.\n\n" +
                    "Customer: " + selectedAccount.getCustomerName() + "\n" +
                    "Account: " + selectedAccount.getAccountNumber() + "\n\n" +
                    "Type 'DELETE' to confirm permanent deletion:"
                );
                
                // Add text field for confirmation
                TextField confirmationField = new TextField();
                confirmationField.setPromptText("Type DELETE here");
                
                VBox dialogContent = new VBox(10);
                dialogContent.getChildren().addAll(
                    new Label("This is your FINAL confirmation.\n\n" +
                              "Customer: " + selectedAccount.getCustomerName() + "\n" +
                              "Account: " + selectedAccount.getAccountNumber() + "\n\n" +
                              "Type 'DELETE' to confirm permanent deletion:"),
                    confirmationField
                );
                
                secondConfirm.getDialogPane().setContent(dialogContent);
                
                secondConfirm.showAndWait().ifPresent(finalResponse -> {
                    if (finalResponse == ButtonType.OK) {
                        String typedText = confirmationField.getText().trim();
                        if ("DELETE".equals(typedText)) {
                            // Proceed with deletion
                            if (deleteCustomerCompletely(selectedAccount.getAccountNumber())) {
                                StaffService.logActivity(
                                    currentStaff.getStaffId(), 
                                    "DELETE_CUSTOMER", 
                                    selectedAccount.getAccountNumber(), 
                                    "Customer permanently deleted: " + selectedAccount.getCustomerName() + 
                                    " (Balance: ৳" + String.format("%.2f", selectedAccount.getBalance()) + ")"
                                );
                                
                                showSuccess(
                                    "✅ Customer Deleted Successfully\n\n" +
                                    "Account " + selectedAccount.getAccountNumber() + " and all related data\n" +
                                    "have been permanently removed from the database."
                                );
                                
                                handleCloseDetails();
                                loadCustomers();
                            }
                        } else {
                            showError("Deletion cancelled. You must type 'DELETE' exactly to confirm.");
                        }
                    }
                });
            }
        });
    }
    
    /**
     * Delete customer completely from database
     * This removes all customer data including transactions
     */
    private boolean deleteCustomerCompletely(int accountNumber) {
        Connection conn = null;
        boolean success = false;
        
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Delete all transactions for this account
            String deleteTransactionsSql = "DELETE FROM transactions WHERE accountNumber = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteTransactionsSql)) {
                pstmt.setInt(1, accountNumber);
                int transactionsDeleted = pstmt.executeUpdate();
                System.out.println("Deleted " + transactionsDeleted + " transactions for account " + accountNumber);
            }
            
            // 2. Delete from account_requests if exists
            String deleteRequestsSql = "DELETE FROM account_requests WHERE accountNumber = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteRequestsSql)) {
                pstmt.setInt(1, accountNumber);
                int requestsDeleted = pstmt.executeUpdate();
                System.out.println("Deleted " + requestsDeleted + " account requests for account " + accountNumber);
            } catch (Exception e) {
                // Table might not exist, continue
                System.out.println("account_requests table might not exist: " + e.getMessage());
            }
            
            // 3. Delete any cheque records if exists
            String deleteChequeSql = "DELETE FROM cheques WHERE accountNumber = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteChequeSql)) {
                pstmt.setInt(1, accountNumber);
                int chequesDeleted = pstmt.executeUpdate();
                System.out.println("Deleted " + chequesDeleted + " cheques for account " + accountNumber);
            } catch (Exception e) {
                // Table might not exist, continue
                System.out.println("cheques table might not exist: " + e.getMessage());
            }
            
            // 4. Delete any notifications for this account
            String deleteNotificationsSql = "DELETE FROM notifications WHERE accountNumber = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteNotificationsSql)) {
                pstmt.setInt(1, accountNumber);
                int notificationsDeleted = pstmt.executeUpdate();
                System.out.println("Deleted " + notificationsDeleted + " notifications for account " + accountNumber);
            } catch (Exception e) {
                // Table might not exist, continue
                System.out.println("notifications table might not exist: " + e.getMessage());
            }
            
            // 5. Delete from users table (login credentials)
            String deleteUserSql = "DELETE FROM users WHERE accountNumber = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSql)) {
                pstmt.setInt(1, accountNumber);
                int usersDeleted = pstmt.executeUpdate();
                System.out.println("Deleted " + usersDeleted + " user records for account " + accountNumber);
            } catch (Exception e) {
                // Table might not exist, continue
                System.out.println("users table might not exist: " + e.getMessage());
            }
            
            // 6. Finally, delete the account itself
            String deleteAccountSql = "DELETE FROM accounts WHERE accountNumber = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAccountSql)) {
                pstmt.setInt(1, accountNumber);
                int accountsDeleted = pstmt.executeUpdate();
                
                if (accountsDeleted > 0) {
                    System.out.println("✅ Successfully deleted account " + accountNumber);
                    conn.commit(); // Commit all changes
                    success = true;
                } else {
                    System.err.println("❌ No account found with number " + accountNumber);
                    conn.rollback();
                    showError("Account not found in database.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error deleting customer: " + e.getMessage());
            e.printStackTrace();
            
            // Rollback transaction on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            
            showError("Error deleting customer: " + e.getMessage() + "\n\nAll changes have been rolled back.");
            success = false;
            
        } finally {
            // Restore auto-commit
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        
        return success;
    }
    
    /**
     * Handle back button
     */
    @FXML
    private void handleBack() {
        Main.showStaffDashboard();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show info message
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
