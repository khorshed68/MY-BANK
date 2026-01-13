package com.mybank.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controller for Admin Customer Oversight.
 * Allows admins to manage customer accounts.
 */
public class AdminCustomerOversightController {
    
    @FXML
    private TableView<AccountRecord> accountsTable;
    
    @FXML
    private TableColumn<AccountRecord, String> accountNumberColumn;
    
    @FXML
    private TableColumn<AccountRecord, String> ownerNameColumn;
    
    @FXML
    private TableColumn<AccountRecord, String> accountTypeColumn;
    
    @FXML
    private TableColumn<AccountRecord, String> balanceColumn;
    
    @FXML
    private TableColumn<AccountRecord, String> statusColumn;
    
    @FXML
    private TableColumn<AccountRecord, String> createdDateColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> filterComboBox;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private Label totalAccountsLabel;
    
    @FXML
    private Label activeAccountsLabel;
    
    @FXML
    private Label blockedAccountsLabel;
    
    @FXML
    private Label totalBalanceLabel;
    
    private ObservableList<AccountRecord> accountsList = FXCollections.observableArrayList();
    private Admin currentAdmin;
    
    @FXML
    private void initialize() {
        currentAdmin = AdminService.getCurrentAdmin();
        
        if (currentAdmin == null) {
            Main.showAdminLogin();
            return;
        }
        
        setupTable();
        setupFilters();
        loadAccountData();
        updateStatistics();
    }
    
    private void setupTable() {
        accountNumberColumn.setCellValueFactory(data -> data.getValue().accountNumberProperty());
        ownerNameColumn.setCellValueFactory(data -> data.getValue().ownerNameProperty());
        accountTypeColumn.setCellValueFactory(data -> data.getValue().accountTypeProperty());
        balanceColumn.setCellValueFactory(data -> data.getValue().balanceProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        createdDateColumn.setCellValueFactory(data -> data.getValue().createdDateProperty());
    }
    
    private void setupFilters() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All", "ACTIVE", "BLOCKED", "PENDING", "CLOSED", "SAVINGS", "CURRENT"
        ));
        filterComboBox.setValue("All");
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAccountData());
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterAccountData());
    }
    
    private void loadAccountData() {
        accountsList.clear();
        
        try {
            Connection conn = DatabaseHelper.getConnection();
            String sql = "SELECT accountNumber, ownerName, accountType, balance, status, " +
                       "COALESCE(createdDate, 'N/A') as createdDate FROM accounts ORDER BY accountNumber DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                accountsList.add(new AccountRecord(
                    rs.getInt("accountNumber"),
                    rs.getString("ownerName"),
                    rs.getString("accountType"),
                    rs.getDouble("balance"),
                    rs.getString("status"),
                    rs.getString("createdDate")
                ));
            }
            
            accountsTable.setItems(accountsList);
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load account data: " + e.getMessage());
        }
    }
    
    private void filterAccountData() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filter = filterComboBox.getValue();
        
        ObservableList<AccountRecord> filtered = FXCollections.observableArrayList();
        
        for (AccountRecord account : accountsList) {
            boolean matchesSearch = searchText.isEmpty() || 
                String.valueOf(account.getAccountNumber()).contains(searchText) ||
                account.getOwnerName().toLowerCase().contains(searchText);
            
            boolean matchesFilter = filter.equals("All") ||
                account.getStatus().equals(filter) ||
                account.getAccountType().equals(filter);
            
            if (matchesSearch && matchesFilter) {
                filtered.add(account);
            }
        }
        
        accountsTable.setItems(filtered);
    }
    
    private void updateStatistics() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Total accounts
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM accounts");
            if (rs.next()) totalAccountsLabel.setText(String.valueOf(rs.getInt(1)));
            
            // Active accounts
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM accounts WHERE status = 'ACTIVE'");
            if (rs.next()) activeAccountsLabel.setText(String.valueOf(rs.getInt(1)));
            
            // Blocked accounts
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM accounts WHERE status = 'BLOCKED'");
            if (rs.next()) blockedAccountsLabel.setText(String.valueOf(rs.getInt(1)));
            
            // Total balance
            rs = conn.createStatement().executeQuery("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE status = 'ACTIVE'");
            if (rs.next()) totalBalanceLabel.setText(String.format("৳ %.2f", rs.getDouble(1)));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBlockAccount() {
        AccountRecord selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an account");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Block");
        confirm.setHeaderText("Block Account");
        confirm.setContentText("Are you sure you want to block account " + selected.getAccountNumber() + "?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateAccountStatus(selected.getAccountNumber(), "BLOCKED");
        }
    }
    
    @FXML
    private void handleUnblockAccount() {
        AccountRecord selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an account");
            return;
        }
        
        updateAccountStatus(selected.getAccountNumber(), "ACTIVE");
    }
    
    @FXML
    private void handleApproveAccount() {
        AccountRecord selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an account");
            return;
        }
        
        if (!selected.getStatus().equals("PENDING")) {
            showError("Only pending accounts can be approved");
            return;
        }
        
        updateAccountStatus(selected.getAccountNumber(), "ACTIVE");
    }
    
    @FXML
    private void handleViewDetails() {
        AccountRecord selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an account");
            return;
        }
        
        // Show account details dialog
        showAccountDetails(selected);
    }
    
    private void updateAccountStatus(int accountNumber, String status) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String sql = "UPDATE accounts SET status = ? WHERE accountNumber = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, accountNumber);
            pstmt.executeUpdate();
            
            AdminService.logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(),
                "STATUS_CHANGE", "ACCOUNT", "Changed account " + accountNumber + " status to " + status, "SUCCESS");
            
            showSuccess("Account status updated to " + status);
            loadAccountData();
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to update account status: " + e.getMessage());
        }
    }
    
    private void showAccountDetails(AccountRecord account) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Account Details");
        details.setHeaderText("Account #" + account.getAccountNumber());
        
        String content = String.format(
            "Owner: %s\nAccount Type: %s\nBalance: ৳ %.2f\nStatus: %s\nCreated: %s",
            account.getOwnerName(), account.getAccountType(), account.getBalance(),
            account.getStatus(), account.getCreatedDate()
        );
        
        details.setContentText(content);
        details.showAndWait();
    }
    
    /**
     * Handle delete customer - permanently removes customer and all related data
     */
    @FXML
    private void handleDeleteCustomer() {
        AccountRecord selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an account to delete");
            return;
        }
        
        // First confirmation - show detailed warning
        Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
        confirmAlert.setTitle("⚠️ Delete Customer Account");
        confirmAlert.setHeaderText("CRITICAL ACTION: Permanently Delete Customer Data");
        confirmAlert.setContentText(
            "You are about to PERMANENTLY DELETE the following customer:\n\n" +
            "Account Number: " + selected.getAccountNumber() + "\n" +
            "Customer Name: " + selected.getOwnerName() + "\n" +
            "Account Type: " + selected.getAccountType() + "\n" +
            "Current Balance: ৳" + String.format("%.2f", selected.getBalance()) + "\n" +
            "Status: " + selected.getStatus() + "\n\n" +
            "⚠️ WARNING: This action will:\n" +
            "• Delete the customer account permanently\n" +
            "• Delete ALL transaction history\n" +
            "• Delete ALL related data from the database\n" +
            "• This action CANNOT be undone!\n\n" +
            "Are you absolutely sure you want to proceed?"
        );
        
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            // Second confirmation - require typing DELETE
            Alert secondConfirm = new Alert(Alert.AlertType.CONFIRMATION);
            secondConfirm.setTitle("Final Confirmation");
            secondConfirm.setHeaderText("Last Chance to Cancel");
            
            TextField confirmationField = new TextField();
            confirmationField.setPromptText("Type DELETE here");
            
            javafx.scene.layout.VBox dialogContent = new javafx.scene.layout.VBox(10);
            dialogContent.getChildren().addAll(
                new Label("This is your FINAL confirmation.\n\n" +
                          "Customer: " + selected.getOwnerName() + "\n" +
                          "Account: " + selected.getAccountNumber() + "\n\n" +
                          "Type 'DELETE' to confirm permanent deletion:"),
                confirmationField
            );
            
            secondConfirm.getDialogPane().setContent(dialogContent);
            
            Optional<ButtonType> finalResult = secondConfirm.showAndWait();
            if (finalResult.isPresent() && finalResult.get() == ButtonType.OK) {
                String typedText = confirmationField.getText().trim();
                if ("DELETE".equals(typedText)) {
                    // Proceed with deletion
                    if (deleteCustomerCompletely(selected.getAccountNumber())) {
                        AdminService.logAuditEvent(
                            "ADMIN", 
                            currentAdmin.getAdminId(), 
                            currentAdmin.getUsername(),
                            "DELETE_CUSTOMER", 
                            "ACCOUNT", 
                            "Permanently deleted customer: " + selected.getOwnerName() + 
                            " (Account: " + selected.getAccountNumber() + 
                            ", Balance: ৳" + String.format("%.2f", selected.getBalance()) + ")",
                            "SUCCESS"
                        );
                        
                        showSuccess(
                            "✅ Customer Deleted Successfully\n\n" +
                            "Account " + selected.getAccountNumber() + " and all related data\n" +
                            "have been permanently removed from the database."
                        );
                        
                        loadAccountData();
                        updateStatistics();
                    }
                } else {
                    showError("Deletion cancelled. You must type 'DELETE' exactly to confirm.");
                }
            }
        }
    }
    
    /**
     * Delete customer completely from database
     * This removes all customer data including transactions, notifications, etc.
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
                System.out.println("account_requests table might not exist: " + e.getMessage());
            }
            
            // 3. Delete any cheque records if exists
            String deleteChequeSql = "DELETE FROM cheques WHERE accountNumber = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteChequeSql)) {
                pstmt.setInt(1, accountNumber);
                int chequesDeleted = pstmt.executeUpdate();
                System.out.println("Deleted " + chequesDeleted + " cheques for account " + accountNumber);
            } catch (Exception e) {
                System.out.println("cheques table might not exist: " + e.getMessage());
            }
            
            // 4. Delete any notifications for this account
            String deleteNotificationsSql = "DELETE FROM notifications WHERE accountNumber = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteNotificationsSql)) {
                pstmt.setInt(1, accountNumber);
                int notificationsDeleted = pstmt.executeUpdate();
                System.out.println("Deleted " + notificationsDeleted + " notifications for account " + accountNumber);
            } catch (Exception e) {
                System.out.println("notifications table might not exist: " + e.getMessage());
            }
            
            // 5. Delete from users table (login credentials)
            String deleteUserSql = "DELETE FROM users WHERE accountNumber = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSql)) {
                pstmt.setInt(1, accountNumber);
                int usersDeleted = pstmt.executeUpdate();
                System.out.println("Deleted " + usersDeleted + " user records for account " + accountNumber);
            } catch (Exception e) {
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
    
    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    public static class AccountRecord {
        private final int accountNumber;
        private final SimpleStringProperty ownerName;
        private final SimpleStringProperty accountType;
        private final double balance;
        private final SimpleStringProperty status;
        private final SimpleStringProperty createdDate;
        
        public AccountRecord(int accountNumber, String ownerName, String accountType, 
                           double balance, String status, String createdDate) {
            this.accountNumber = accountNumber;
            this.ownerName = new SimpleStringProperty(ownerName);
            this.accountType = new SimpleStringProperty(accountType);
            this.balance = balance;
            this.status = new SimpleStringProperty(status);
            this.createdDate = new SimpleStringProperty(createdDate);
        }
        
        public int getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName.get(); }
        public String getAccountType() { return accountType.get(); }
        public double getBalance() { return balance; }
        public String getStatus() { return status.get(); }
        public String getCreatedDate() { return createdDate.get(); }
        
        public SimpleStringProperty accountNumberProperty() { return new SimpleStringProperty(String.valueOf(accountNumber)); }
        public SimpleStringProperty ownerNameProperty() { return ownerName; }
        public SimpleStringProperty accountTypeProperty() { return accountType; }
        public SimpleStringProperty balanceProperty() { return new SimpleStringProperty(String.format("৳ %.2f", balance)); }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty createdDateProperty() { return createdDate; }
    }
}
