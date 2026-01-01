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
import com.mybank.services.NotificationService;
import com.mybank.services.StaffService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
        if (selectedAccount == null) return;
        
        // TODO: Navigate to transaction history view
        showInfo("Transaction history view will be implemented in the next phase");
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
