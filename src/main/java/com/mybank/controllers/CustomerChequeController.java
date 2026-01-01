package com.mybank.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

import com.mybank.database.DatabaseConnection;
import com.mybank.models.Cheque;
import com.mybank.models.ChequeBook;
import com.mybank.models.ChequeTransaction;
import com.mybank.services.NotificationService;
import com.mybank.utils.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class CustomerChequeController {
    
    @FXML private ComboBox<String> accountComboBox;
    @FXML private Label accountBalanceLabel;
    @FXML private Label eligibilityStatusLabel;
    @FXML private Button requestChequeBookBtn;
    
    // Cheque Book Requests Table
    @FXML private TableView<ChequeBook> chequeBookTable;
    @FXML private TableColumn<ChequeBook, String> bookNumberCol;
    @FXML private TableColumn<ChequeBook, String> accountNumCol;
    @FXML private TableColumn<ChequeBook, String> startChequeCol;
    @FXML private TableColumn<ChequeBook, String> endChequeCol;
    @FXML private TableColumn<ChequeBook, Integer> totalLeavesCol;
    @FXML private TableColumn<ChequeBook, Integer> remainingLeavesCol;
    @FXML private TableColumn<ChequeBook, String> statusCol;
    @FXML private TableColumn<ChequeBook, Timestamp> requestDateCol;
    
    // Cheques Table
    @FXML private TableView<Cheque> chequesTable;
    @FXML private TableColumn<Cheque, String> chequeNumberCol;
    @FXML private TableColumn<Cheque, String> chequeBookCol;
    @FXML private TableColumn<Cheque, Double> amountCol;
    @FXML private TableColumn<Cheque, String> payeeCol;
    @FXML private TableColumn<Cheque, Date> issueDateCol;
    @FXML private TableColumn<Cheque, String> chequeStatusCol;
    @FXML private TableColumn<Cheque, Timestamp> depositDateCol;
    @FXML private TableColumn<Cheque, Timestamp> clearanceDateCol;
    
    // Deposit Cheque Section
    @FXML private TextField depositChequeNumberField;
    @FXML private TextField depositAmountField;
    @FXML private TextField depositPayerNameField;
    @FXML private ComboBox<String> depositToAccountComboBox;
    @FXML private DatePicker chequeIssueDatePicker;
    @FXML private TextArea depositRemarksArea;
    @FXML private Button depositChequeBtn;
    
    // Transaction History Table
    @FXML private TableView<ChequeTransaction> transactionTable;
    @FXML private TableColumn<ChequeTransaction, String> txChequeNumberCol;
    @FXML private TableColumn<ChequeTransaction, String> txTypeCol;
    @FXML private TableColumn<ChequeTransaction, String> txOldStatusCol;
    @FXML private TableColumn<ChequeTransaction, String> txNewStatusCol;
    @FXML private TableColumn<ChequeTransaction, Double> txAmountCol;
    @FXML private TableColumn<ChequeTransaction, Timestamp> txDateCol;
    @FXML private TableColumn<ChequeTransaction, String> txRemarksCol;
    
    private int customerId;
    private NotificationService notificationService;
    private ObservableList<ChequeBook> chequeBooks = FXCollections.observableArrayList();
    private ObservableList<Cheque> cheques = FXCollections.observableArrayList();
    private ObservableList<ChequeTransaction> transactions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        customerId = SessionManager.getCurrentCustomerId();
        notificationService = new NotificationService();
        
        setupTables();
        loadCustomerAccounts();
        loadChequeBooks();
        loadCheques();
        loadTransactionHistory();
        
        // Set up account selection listener
        accountComboBox.setOnAction(e -> checkEligibility());
    }
    
    private void setupTables() {
        // Cheque Book Table
        bookNumberCol.setCellValueFactory(new PropertyValueFactory<>("bookNumber"));
        accountNumCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        startChequeCol.setCellValueFactory(new PropertyValueFactory<>("startChequeNumber"));
        endChequeCol.setCellValueFactory(new PropertyValueFactory<>("endChequeNumber"));
        totalLeavesCol.setCellValueFactory(new PropertyValueFactory<>("totalLeaves"));
        remainingLeavesCol.setCellValueFactory(new PropertyValueFactory<>("remainingLeaves"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        requestDateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        
        // Cheques Table
        chequeNumberCol.setCellValueFactory(new PropertyValueFactory<>("chequeNumber"));
        chequeBookCol.setCellValueFactory(new PropertyValueFactory<>("bookNumber"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        payeeCol.setCellValueFactory(new PropertyValueFactory<>("payeeName"));
        issueDateCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        chequeStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        depositDateCol.setCellValueFactory(new PropertyValueFactory<>("depositDate"));
        clearanceDateCol.setCellValueFactory(new PropertyValueFactory<>("clearanceDate"));
        
        // Transaction Table
        txChequeNumberCol.setCellValueFactory(new PropertyValueFactory<>("chequeNumber"));
        txTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        txOldStatusCol.setCellValueFactory(new PropertyValueFactory<>("oldStatus"));
        txNewStatusCol.setCellValueFactory(new PropertyValueFactory<>("newStatus"));
        txAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        txDateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        txRemarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));
    }
    
    private void loadCustomerAccounts() {
        accountComboBox.getItems().clear();
        depositToAccountComboBox.getItems().clear();
        
        String query = "SELECT accountNumber, accountType FROM accounts WHERE accountNumber = ? AND status = 'ACTIVE'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String display = rs.getString("accountNumber") + " - " + rs.getString("accountType");
                accountComboBox.getItems().add(display);
                depositToAccountComboBox.getItems().add(display);
            }
            
            if (!accountComboBox.getItems().isEmpty()) {
                accountComboBox.getSelectionModel().selectFirst();
                depositToAccountComboBox.getSelectionModel().selectFirst();
                checkEligibility();
            }
            
        } catch (SQLException e) {
            showError("Error loading accounts: " + e.getMessage());
        }
    }
    
    private void checkEligibility() {
        String selectedAccount = accountComboBox.getValue();
        if (selectedAccount == null) return;
        
        String accountNumber = selectedAccount.split(" - ")[0];
        
        String query = "SELECT a.accountNumber, a.balance, a.accountType, a.createdDate, " +
                      "e.minimum_balance, e.minimum_account_age_days, e.max_books_per_year, e.leaves_per_book, " +
                      "COALESCE(COUNT(cb.cheque_book_id), 0) as books_this_year " +
                      "FROM accounts a " +
                      "LEFT JOIN cheque_book_eligibility e ON a.accountType = e.account_type " +
                      "LEFT JOIN cheque_books cb ON a.accountNumber = cb.accountNumber " +
                      "AND strftime('%Y', cb.request_date) = strftime('%Y', 'now') " +
                      "WHERE a.accountNumber = ? " +
                      "GROUP BY a.accountNumber";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                double minBalance = rs.getDouble("minimum_balance");
                int accountAgeDays = (int) ((System.currentTimeMillis() - rs.getTimestamp("createdDate").getTime()) / (1000 * 60 * 60 * 24));
                int minAgeDays = rs.getInt("minimum_account_age_days");
                int booksThisYear = rs.getInt("books_this_year");
                int maxBooksPerYear = rs.getInt("max_books_per_year");
                
                accountBalanceLabel.setText(String.format("Balance: TAKA %.2f", balance));
                
                boolean eligible = balance >= minBalance && accountAgeDays >= minAgeDays && booksThisYear < maxBooksPerYear;
                
                if (eligible) {
                    eligibilityStatusLabel.setText("✓ Eligible for Cheque Book");
                    eligibilityStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    requestChequeBookBtn.setDisable(false);
                } else {
                    StringBuilder reason = new StringBuilder("✗ Not Eligible: ");
                    if (balance < minBalance) {
                        reason.append("Minimum balance TAKA ").append(minBalance).append(" required. ");
                    }
                    if (accountAgeDays < minAgeDays) {
                        reason.append("Account must be ").append(minAgeDays).append(" days old. ");
                    }
                    if (booksThisYear >= maxBooksPerYear) {
                        reason.append("Annual limit reached.");
                    }
                    eligibilityStatusLabel.setText(reason.toString());
                    eligibilityStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    requestChequeBookBtn.setDisable(true);
                }
            }
            
        } catch (SQLException e) {
            showError("Error checking eligibility: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRequestChequeBook() {
        String selectedAccount = accountComboBox.getValue();
        if (selectedAccount == null) {
            showError("Please select an account");
            return;
        }
        
        String accountNumber = selectedAccount.split(" - ")[0];
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get account details
            String accountQuery = "SELECT accountNumber, accountType FROM accounts WHERE accountNumber = ?";
            PreparedStatement pstmt = conn.prepareStatement(accountQuery);
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                showError("Account not found");
                return;
            }
            
            int accountId = rs.getInt("accountNumber");
            String accountType = rs.getString("accountType");
            
            // Get leaves per book from eligibility
            String eligibilityQuery = "SELECT leaves_per_book FROM cheque_book_eligibility WHERE account_type = ?";
            pstmt = conn.prepareStatement(eligibilityQuery);
            pstmt.setString(1, accountType);
            rs = pstmt.executeQuery();
            
            int leavesPerBook = 25; // default
            if (rs.next()) {
                leavesPerBook = rs.getInt("leaves_per_book");
            }
            
            // Generate unique book number
            String bookNumber = generateBookNumber();
            String startChequeNumber = generateChequeNumber();
            long startNum = Long.parseLong(startChequeNumber);
            String endChequeNumber = String.format("%015d", startNum + leavesPerBook - 1);
            
            // Insert cheque book request
            String insertQuery = "INSERT INTO cheque_books (accountNumber, book_number, " +
                               "start_cheque_number, end_cheque_number, total_leaves, remaining_leaves, status) " +
                               "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
            
            pstmt = conn.prepareStatement(insertQuery);
            pstmt.setInt(1, accountId);
            pstmt.setString(2, bookNumber);
            pstmt.setString(3, startChequeNumber);
            pstmt.setString(4, endChequeNumber);
            pstmt.setInt(5, leavesPerBook);
            pstmt.setInt(6, leavesPerBook);
            
            pstmt.executeUpdate();
            
            // Send cheque book request notification email
            try {
                notificationService.sendChequeBookRequestNotification(
                    accountId, 
                    bookNumber, 
                    leavesPerBook
                );
            } catch (Exception e) {
                System.err.println("Failed to send cheque book request notification: " + e.getMessage());
            }
            
            showSuccess("Cheque book request submitted successfully!\nBook Number: " + bookNumber);
            loadChequeBooks();
            checkEligibility();
            
        } catch (SQLException e) {
            showError("Error requesting cheque book: " + e.getMessage());
        }
    }
    
    private void loadChequeBooks() {
        chequeBooks.clear();
        
        String query = "SELECT * FROM vw_cheque_book_summary WHERE accountNumber = ? ORDER BY request_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ChequeBook book = new ChequeBook();
                book.setChequeBookId(rs.getInt("cheque_book_id"));
                book.setBookNumber(rs.getString("book_number"));
                book.setAccountId(rs.getInt("accountNumber"));
                book.setAccountNumber(String.valueOf(rs.getInt("accountNumber")));
                book.setCustomerId(rs.getInt("accountNumber"));
                book.setCustomerName(rs.getString("customer_name"));
                book.setStartChequeNumber(rs.getString("start_cheque_number"));
                book.setEndChequeNumber(rs.getString("end_cheque_number"));
                book.setTotalLeaves(rs.getInt("total_leaves"));
                book.setRemainingLeaves(rs.getInt("remaining_leaves"));
                book.setStatus(rs.getString("status"));
                book.setRequestDate(rs.getTimestamp("request_date"));
                book.setApprovalDate(rs.getTimestamp("approval_date"));
                
                chequeBooks.add(book);
            }
            
            chequeBookTable.setItems(chequeBooks);
            
        } catch (SQLException e) {
            showError("Error loading cheque books: " + e.getMessage());
        }
    }
    
    private void loadCheques() {
        cheques.clear();
        
        String query = "SELECT * FROM vw_cheque_details WHERE accountNumber = ? ORDER BY issue_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Cheque cheque = new Cheque();
                cheque.setChequeId(rs.getInt("cheque_id"));
                cheque.setChequeNumber(rs.getString("cheque_number"));
                cheque.setAccountId(rs.getInt("accountNumber"));
                cheque.setAccountNumber(String.valueOf(rs.getInt("accountNumber")));
                cheque.setCustomerId(rs.getInt("accountNumber"));
                cheque.setCustomerName(rs.getString("account_holder"));
                cheque.setAmount(rs.getDouble("amount"));
                cheque.setPayeeName(rs.getString("payee_name"));
                cheque.setIssueDate(rs.getDate("issue_date"));
                cheque.setDepositDate(rs.getTimestamp("deposit_date"));
                cheque.setClearanceDate(rs.getTimestamp("clearance_date"));
                cheque.setStatus(rs.getString("status"));
                cheque.setBounceReason(rs.getString("bounce_reason"));
                cheque.setBookNumber(rs.getString("book_number"));
                
                cheques.add(cheque);
            }
            
            chequesTable.setItems(cheques);
            
        } catch (SQLException e) {
            showError("Error loading cheques: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDepositCheque() {
        String chequeNumber = depositChequeNumberField.getText().trim();
        String amountStr = depositAmountField.getText().trim();
        String payerName = depositPayerNameField.getText().trim();
        String selectedAccount = depositToAccountComboBox.getValue();
        LocalDate issueDate = chequeIssueDatePicker.getValue();
        String remarks = depositRemarksArea.getText().trim();
        
        if (chequeNumber.isEmpty() || amountStr.isEmpty() || payerName.isEmpty() || 
            selectedAccount == null || issueDate == null) {
            showError("Please fill all required fields");
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showError("Amount must be positive");
                return;
            }
            
            String accountNumber = selectedAccount.split(" - ")[0];
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Get account ID
                String accountQuery = "SELECT accountNumber FROM accounts WHERE accountNumber = ?";
                PreparedStatement pstmt = conn.prepareStatement(accountQuery);
                pstmt.setString(1, accountNumber);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    showError("Account not found");
                    return;
                }
                
                int depositToAccountId = rs.getInt("accountNumber");
                
                // Check if cheque already exists
                String checkQuery = "SELECT cheque_id, status FROM cheques WHERE cheque_number = ?";
                pstmt = conn.prepareStatement(checkQuery);
                pstmt.setString(1, chequeNumber);
                rs = pstmt.executeQuery();
                
                int chequeId;
                
                if (rs.next()) {
                    // Cheque exists, update it
                    chequeId = rs.getInt("cheque_id");
                    String currentStatus = rs.getString("status");
                    
                    if (!currentStatus.equals("ISSUED")) {
                        showError("This cheque has already been deposited or processed");
                        return;
                    }
                    
                    String updateQuery = "UPDATE cheques SET amount = ?, payee_name = ?, " +
                                       "deposit_date = CURRENT_TIMESTAMP, deposited_to_account = ?, " +
                                       "status = 'DEPOSITED', remarks = ? " +
                                       "WHERE cheque_id = ?";
                    
                    pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setDouble(1, amount);
                    pstmt.setString(2, payerName);
                    pstmt.setInt(3, depositToAccountId);
                    pstmt.setString(4, remarks);
                    pstmt.setInt(5, chequeId);
                    pstmt.executeUpdate();
                    
                    // Log transaction
                    logChequeTransaction(conn, chequeId, chequeNumber, depositToAccountId, 
                                       "DEPOSIT", "ISSUED", "DEPOSITED", amount, customerId, "CUSTOMER", remarks);
                    
                    // Send deposit notification email
                    try {
                        notificationService.sendChequeDepositedNotification(
                            depositToAccountId, 
                            chequeNumber, 
                            amount,
                            payerName
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to send cheque deposited notification: " + e.getMessage());
                    }
                    
                } else {
                    // New cheque deposit (from another bank or external)
                    showError("Cheque number not found in our system. Please verify the cheque number.");
                    return;
                }
                
                showSuccess("Cheque deposited successfully!\nPending staff verification and clearance.");
                clearDepositFields();
                loadCheques();
                loadTransactionHistory();
                
            } catch (SQLException e) {
                showError("Error depositing cheque: " + e.getMessage());
            }
            
        } catch (NumberFormatException e) {
            showError("Invalid amount");
        }
    }
    
    private void loadTransactionHistory() {
        transactions.clear();
        
        String query = "SELECT * FROM vw_cheque_transaction_history " +
                      "WHERE accountNumber = ? " +
                      "ORDER BY transaction_date DESC LIMIT 100";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ChequeTransaction tx = new ChequeTransaction();
                tx.setTransactionId(rs.getInt("transaction_id"));
                tx.setChequeNumber(rs.getString("cheque_number"));
                tx.setTransactionType(rs.getString("transaction_type"));
                tx.setOldStatus(rs.getString("old_status"));
                tx.setNewStatus(rs.getString("new_status"));
                tx.setAmount(rs.getDouble("amount"));
                tx.setTransactionDate(rs.getTimestamp("transaction_date"));
                tx.setRemarks(rs.getString("remarks"));
                tx.setAccountNumber(String.valueOf(rs.getInt("accountNumber")));
                tx.setPerformedByName(rs.getString("performed_by_name"));
                
                transactions.add(tx);
            }
            
            transactionTable.setItems(transactions);
            
        } catch (SQLException e) {
            showError("Error loading transaction history: " + e.getMessage());
        }
    }
    
    private void logChequeTransaction(Connection conn, int chequeId, String chequeNumber, 
                                     int accountId, String transactionType, String oldStatus, 
                                     String newStatus, double amount, int performedBy, 
                                     String userType, String remarks) throws SQLException {
        String query = "INSERT INTO cheque_transactions (cheque_id, cheque_number, accountNumber, " +
                      "transaction_type, old_status, new_status, amount, performed_by, user_type, remarks) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, chequeId);
        pstmt.setString(2, chequeNumber);
        pstmt.setInt(3, accountId);
        pstmt.setString(4, transactionType);
        pstmt.setString(5, oldStatus);
        pstmt.setString(6, newStatus);
        pstmt.setDouble(7, amount);
        pstmt.setInt(8, performedBy);
        pstmt.setString(9, userType);
        pstmt.setString(10, remarks);
        pstmt.executeUpdate();
    }
    
    private String generateBookNumber() {
        return "BK" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    private String generateChequeNumber() {
        return String.format("%015d", (long)(Math.random() * 900000000000000L) + 100000000000000L);
    }
    
    private void clearDepositFields() {
        depositChequeNumberField.clear();
        depositAmountField.clear();
        depositPayerNameField.clear();
        depositRemarksArea.clear();
        chequeIssueDatePicker.setValue(null);
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) accountComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Customer Dashboard");
            
        } catch (IOException e) {
            showError("Error returning to dashboard: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadChequeBooks();
        loadCheques();
        loadTransactionHistory();
        checkEligibility();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
