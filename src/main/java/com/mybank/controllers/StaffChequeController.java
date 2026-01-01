package com.mybank.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.mybank.database.DatabaseConnection;
import com.mybank.models.Cheque;
import com.mybank.models.ChequeBook;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class StaffChequeController {
    
    // Pending Cheque Book Requests
    @FXML private TableView<ChequeBook> pendingRequestsTable;
    @FXML private TableColumn<ChequeBook, String> reqBookNumberCol;
    @FXML private TableColumn<ChequeBook, String> reqAccountCol;
    @FXML private TableColumn<ChequeBook, String> reqCustomerCol;
    @FXML private TableColumn<ChequeBook, Integer> reqLeavesCol;
    @FXML private TableColumn<ChequeBook, Timestamp> reqDateCol;
    @FXML private TableColumn<ChequeBook, String> reqAccountTypeCol;
    @FXML private TableColumn<ChequeBook, Double> reqBalanceCol;
    
    @FXML private TextArea approvalRemarksArea;
    @FXML private TextArea rejectionReasonArea;
    @FXML private Button approveRequestBtn;
    @FXML private Button rejectRequestBtn;
    
    // Deposited Cheques (Pending Clearance)
    @FXML private TableView<Cheque> depositedChequesTable;
    @FXML private TableColumn<Cheque, String> depChequeNumCol;
    @FXML private TableColumn<Cheque, String> depAccountCol;
    @FXML private TableColumn<Cheque, String> depPayerCol;
    @FXML private TableColumn<Cheque, Double> depAmountCol;
    @FXML private TableColumn<Cheque, Date> depIssueDateCol;
    @FXML private TableColumn<Cheque, Timestamp> depDepositDateCol;
    @FXML private TableColumn<Cheque, String> depDepositedByCol;
    @FXML private TableColumn<Cheque, Double> depCurrentBalanceCol;
    
    @FXML private CheckBox signatureVerifiedCheck;
    @FXML private TextArea clearanceRemarksArea;
    @FXML private TextArea bounceReasonArea;
    @FXML private Button clearChequeBtn;
    @FXML private Button bounceChequeBtn;
    
    // All Cheque Books
    @FXML private TableView<ChequeBook> allBooksTable;
    @FXML private TableColumn<ChequeBook, String> allBookNumberCol;
    @FXML private TableColumn<ChequeBook, String> allAccountCol;
    @FXML private TableColumn<ChequeBook, String> allCustomerCol;
    @FXML private TableColumn<ChequeBook, String> allStatusCol;
    @FXML private TableColumn<ChequeBook, Integer> allRemainingCol;
    @FXML private TableColumn<ChequeBook, Timestamp> allApprovalDateCol;
    
    @FXML private TextField searchBookField;
    @FXML private ComboBox<String> statusFilterCombo;
    
    // All Cheques
    @FXML private TableView<Cheque> allChequesTable;
    @FXML private TableColumn<Cheque, String> allChequeNumCol;
    @FXML private TableColumn<Cheque, String> allChequeAccountCol;
    @FXML private TableColumn<Cheque, Double> allChequeAmountCol;
    @FXML private TableColumn<Cheque, String> allChequePayeeCol;
    @FXML private TableColumn<Cheque, String> allChequeStatusCol;
    @FXML private TableColumn<Cheque, Timestamp> allChequeClearanceDateCol;
    
    @FXML private TextField searchChequeField;
    @FXML private ComboBox<String> chequeStatusFilterCombo;
    
    // Statistics
    @FXML private Label totalRequestsLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label approvedBooksLabel;
    @FXML private Label activeBooksLabel;
    @FXML private Label totalChequesLabel;
    @FXML private Label clearedChequesLabel;
    @FXML private Label bouncedChequesLabel;
    @FXML private Label pendingClearanceLabel;
    
    private int staffId;
    private NotificationService notificationService;
    private ObservableList<ChequeBook> pendingRequests = FXCollections.observableArrayList();
    private ObservableList<Cheque> depositedCheques = FXCollections.observableArrayList();
    private ObservableList<ChequeBook> allBooks = FXCollections.observableArrayList();
    private ObservableList<Cheque> allCheques = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        staffId = SessionManager.getCurrentStaffId();
        notificationService = new NotificationService();
        
        setupTables();
        loadPendingRequests();
        loadDepositedCheques();
        loadAllChequeBooks();
        loadAllCheques();
        loadStatistics();
        
        // Setup filters
        statusFilterCombo.setItems(FXCollections.observableArrayList(
            "ALL", "PENDING", "APPROVED", "ISSUED", "COMPLETED", "REJECTED", "CANCELLED"
        ));
        statusFilterCombo.setValue("ALL");
        
        chequeStatusFilterCombo.setItems(FXCollections.observableArrayList(
            "ALL", "ISSUED", "DEPOSITED", "PENDING_CLEARANCE", "CLEARED", "BOUNCED", "CANCELLED"
        ));
        chequeStatusFilterCombo.setValue("ALL");
        
        statusFilterCombo.setOnAction(e -> filterChequeBooks());
        chequeStatusFilterCombo.setOnAction(e -> filterCheques());
    }
    
    private void setupTables() {
        // Pending Requests Table
        reqBookNumberCol.setCellValueFactory(new PropertyValueFactory<>("bookNumber"));
        reqAccountCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        reqCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        reqLeavesCol.setCellValueFactory(new PropertyValueFactory<>("totalLeaves"));
        reqDateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        reqAccountTypeCol.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        reqBalanceCol.setCellValueFactory(new PropertyValueFactory<>("currentBalance"));
        
        // Deposited Cheques Table
        depChequeNumCol.setCellValueFactory(new PropertyValueFactory<>("chequeNumber"));
        depAccountCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        depPayerCol.setCellValueFactory(new PropertyValueFactory<>("payeeName"));
        depAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        depIssueDateCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        depDepositDateCol.setCellValueFactory(new PropertyValueFactory<>("depositDate"));
        depDepositedByCol.setCellValueFactory(new PropertyValueFactory<>("depositedByName"));
        depCurrentBalanceCol.setCellValueFactory(new PropertyValueFactory<>("currentBalance"));
        
        // All Books Table
        allBookNumberCol.setCellValueFactory(new PropertyValueFactory<>("bookNumber"));
        allAccountCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        allCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        allStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        allRemainingCol.setCellValueFactory(new PropertyValueFactory<>("remainingLeaves"));
        allApprovalDateCol.setCellValueFactory(new PropertyValueFactory<>("approvalDate"));
        
        // All Cheques Table
        allChequeNumCol.setCellValueFactory(new PropertyValueFactory<>("chequeNumber"));
        allChequeAccountCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        allChequeAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        allChequePayeeCol.setCellValueFactory(new PropertyValueFactory<>("payeeName"));
        allChequeStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        allChequeClearanceDateCol.setCellValueFactory(new PropertyValueFactory<>("clearanceDate"));
    }
    
    private void loadPendingRequests() {
        pendingRequests.clear();
        
        String query = "SELECT * FROM vw_cheque_book_summary WHERE status = 'PENDING' ORDER BY request_date ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
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
                book.setAccountType(rs.getString("account_type"));
                book.setCurrentBalance(rs.getDouble("current_balance"));
                
                pendingRequests.add(book);
            }
            
            pendingRequestsTable.setItems(pendingRequests);
            
        } catch (SQLException e) {
            showError("Error loading pending requests: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleApproveRequest() {
        ChequeBook selected = pendingRequestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a cheque book request to approve");
            return;
        }
        
        String remarks = approvalRemarksArea.getText().trim();
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Approval");
        confirm.setHeaderText("Approve Cheque Book Request");
        confirm.setContentText("Book Number: " + selected.getBookNumber() + 
                              "\nCustomer: " + selected.getCustomerName() + 
                              "\nAccount: " + selected.getAccountNumber() + 
                              "\n\nAre you sure you want to approve this request?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    // Update cheque book status
                    String updateQuery = "UPDATE cheque_books SET status = 'APPROVED', " +
                                       "approval_date = CURRENT_TIMESTAMP, approved_by = ? " +
                                       "WHERE cheque_book_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setInt(1, staffId);
                    pstmt.setInt(2, selected.getChequeBookId());
                    pstmt.executeUpdate();
                    
                    // Create individual cheques
                    long startNum = Long.parseLong(selected.getStartChequeNumber());
                    String insertChequeQuery = "INSERT INTO cheques (cheque_book_id, accountNumber, " +
                                              "cheque_number, status) VALUES (?, ?, ?, 'ISSUED')";
                    pstmt = conn.prepareStatement(insertChequeQuery);
                    
                    for (int i = 0; i < selected.getTotalLeaves(); i++) {
                        String chequeNum = String.format("%015d", startNum + i);
                        pstmt.setInt(1, selected.getChequeBookId());
                        pstmt.setInt(2, selected.getAccountId());
                        pstmt.setString(3, chequeNum);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    
                    // Update book status to ISSUED
                    updateQuery = "UPDATE cheque_books SET status = 'ISSUED' WHERE cheque_book_id = ?";
                    pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setInt(1, selected.getChequeBookId());
                    pstmt.executeUpdate();
                    
                    conn.commit();
                    
                    // Send approval notification email
                    try {
                        notificationService.sendChequeBookApprovalNotification(
                            selected.getAccountId(), 
                            selected.getBookNumber(), 
                            selected.getTotalLeaves(),
                            selected.getStartChequeNumber(),
                            selected.getEndChequeNumber()
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to send cheque book approval notification: " + e.getMessage());
                    }
                    
                    showSuccess("Cheque book approved and issued successfully!\n" +
                              selected.getTotalLeaves() + " cheques have been created.");
                    
                    approvalRemarksArea.clear();
                    loadPendingRequests();
                    loadAllChequeBooks();
                    loadAllCheques();
                    loadStatistics();
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
                
            } catch (SQLException e) {
                showError("Error approving request: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRejectRequest() {
        ChequeBook selected = pendingRequestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a cheque book request to reject");
            return;
        }
        
        String reason = rejectionReasonArea.getText().trim();
        if (reason.isEmpty()) {
            showError("Please provide a reason for rejection");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Rejection");
        confirm.setHeaderText("Reject Cheque Book Request");
        confirm.setContentText("Book Number: " + selected.getBookNumber() + 
                              "\nCustomer: " + selected.getCustomerName() + 
                              "\nReason: " + reason + 
                              "\n\nAre you sure you want to reject this request?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            String updateQuery = "UPDATE cheque_books SET status = 'REJECTED', " +
                               "rejection_reason = ?, approved_by = ?, approval_date = CURRENT_TIMESTAMP " +
                               "WHERE cheque_book_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                
                pstmt.setString(1, reason);
                pstmt.setInt(2, staffId);
                pstmt.setInt(3, selected.getChequeBookId());
                pstmt.executeUpdate();
                
                // Send rejection notification email
                try {
                    notificationService.sendChequeBookRejectionNotification(
                        selected.getAccountId(), 
                        selected.getBookNumber(), 
                        reason
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send cheque book rejection notification: " + e.getMessage());
                }
                
                showSuccess("Cheque book request rejected");
                
                rejectionReasonArea.clear();
                loadPendingRequests();
                loadStatistics();
                
            } catch (SQLException e) {
                showError("Error rejecting request: " + e.getMessage());
            }
        }
    }
    
    private void loadDepositedCheques() {
        depositedCheques.clear();
        
        String query = "SELECT * FROM vw_cheque_details WHERE status IN ('DEPOSITED', 'PENDING_CLEARANCE') " +
                      "ORDER BY deposit_date ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
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
                cheque.setStatus(rs.getString("status"));
                cheque.setBookNumber(rs.getString("book_number"));
                cheque.setSignatureVerified(rs.getBoolean("signature_verified"));
                
                depositedCheques.add(cheque);
            }
            
            depositedChequesTable.setItems(depositedCheques);
            
        } catch (SQLException e) {
            showError("Error loading deposited cheques: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearCheque() {
        Cheque selected = depositedChequesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a cheque to clear");
            return;
        }
        
        if (!signatureVerifiedCheck.isSelected()) {
            showError("Please verify the signature before clearing the cheque");
            return;
        }
        
        String remarks = clearanceRemarksArea.getText().trim();
        
        // Check if account has sufficient balance
        if (selected.getCurrentBalance() < selected.getAmount()) {
            Alert confirm = new Alert(Alert.AlertType.WARNING);
            confirm.setTitle("Insufficient Balance");
            confirm.setHeaderText("Account has insufficient balance");
            confirm.setContentText("Account Balance: TAKA " + selected.getCurrentBalance() + 
                                  "\nCheque Amount: TAKA " + selected.getAmount() + 
                                  "\n\nThe cheque should be bounced. Do you want to proceed with clearance anyway?");
            
            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Clearance");
        confirm.setHeaderText("Clear Cheque");
        confirm.setContentText("Cheque Number: " + selected.getChequeNumber() + 
                              "\nAmount: TAKA " + selected.getAmount() + 
                              "\nAccount: " + selected.getAccountNumber() + 
                              "\n\nAre you sure you want to clear this cheque?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    // Update cheque status
                    String updateChequeQuery = "UPDATE cheques SET status = 'CLEARED', " +
                                             "clearance_date = CURRENT_TIMESTAMP, processed_by = ?, " +
                                             "signature_verified = TRUE, remarks = ? " +
                                             "WHERE cheque_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(updateChequeQuery);
                    pstmt.setInt(1, staffId);
                    pstmt.setString(2, remarks);
                    pstmt.setInt(3, selected.getChequeId());
                    pstmt.executeUpdate();
                    
                    // Deduct amount from issuer's account
                    String deductQuery = "UPDATE accounts SET balance = balance - ? WHERE accountNumber = ?";
                    pstmt = conn.prepareStatement(deductQuery);
                    pstmt.setDouble(1, selected.getAmount());
                    pstmt.setInt(2, selected.getAccountId());
                    pstmt.executeUpdate();
                    
                    // Add amount to depositor's account if within the bank
                    if (selected.getDepositedByAccount() != null) {
                        String addQuery = "UPDATE accounts SET balance = balance + ? WHERE accountNumber = ?";
                        pstmt = conn.prepareStatement(addQuery);
                        pstmt.setDouble(1, selected.getAmount());
                        pstmt.setInt(2, selected.getDepositedByAccount());
                        pstmt.executeUpdate();
                    }
                    
                    // Log transaction
                    logChequeTransaction(conn, selected.getChequeId(), selected.getChequeNumber(),
                                       selected.getAccountId(), "CLEAR", selected.getStatus(), "CLEARED",
                                       selected.getAmount(), staffId, "STAFF", remarks);
                    
                    // Get new balance for notification
                    String balanceQuery = "SELECT balance FROM accounts WHERE accountNumber = ?";
                    pstmt = conn.prepareStatement(balanceQuery);
                    pstmt.setInt(1, selected.getAccountId());
                    ResultSet rs = pstmt.executeQuery();
                    double newBalance = 0;
                    if (rs.next()) {
                        newBalance = rs.getDouble("balance");
                    }
                    
                    conn.commit();
                    
                    // Send clearance notification email
                    try {
                        notificationService.sendChequeClearedNotification(
                            selected.getAccountId(), 
                            selected.getChequeNumber(), 
                            selected.getAmount(),
                            newBalance
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to send cheque cleared notification: " + e.getMessage());
                    }
                    
                    showSuccess("Cheque cleared successfully!\nAmount TAKA " + selected.getAmount() + " transferred.");
                    
                    clearanceRemarksArea.clear();
                    signatureVerifiedCheck.setSelected(false);
                    loadDepositedCheques();
                    loadAllCheques();
                    loadStatistics();
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
                
            } catch (SQLException e) {
                showError("Error clearing cheque: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleBounceCheque() {
        Cheque selected = depositedChequesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a cheque to bounce");
            return;
        }
        
        String bounceReason = bounceReasonArea.getText().trim();
        if (bounceReason.isEmpty()) {
            showError("Please provide a reason for bouncing the cheque");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Bounce");
        confirm.setHeaderText("Bounce Cheque");
        confirm.setContentText("Cheque Number: " + selected.getChequeNumber() + 
                              "\nAmount: TAKA " + selected.getAmount() + 
                              "\nReason: " + bounceReason + 
                              "\n\nAre you sure you want to bounce this cheque?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    // Update cheque status
                    String updateQuery = "UPDATE cheques SET status = 'BOUNCED', " +
                                       "bounce_reason = ?, processed_by = ?, " +
                                       "clearance_date = CURRENT_TIMESTAMP " +
                                       "WHERE cheque_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setString(1, bounceReason);
                    pstmt.setInt(2, staffId);
                    pstmt.setInt(3, selected.getChequeId());
                    pstmt.executeUpdate();
                    
                    // Log transaction
                    logChequeTransaction(conn, selected.getChequeId(), selected.getChequeNumber(),
                                       selected.getAccountId(), "BOUNCE", selected.getStatus(), "BOUNCED",
                                       selected.getAmount(), staffId, "STAFF", bounceReason);
                    
                    conn.commit();
                    
                    // Send bounce notification email
                    try {
                        notificationService.sendChequeBouncedNotification(
                            selected.getAccountId(), 
                            selected.getChequeNumber(), 
                            selected.getAmount(),
                            bounceReason
                        );
                    } catch (Exception e) {
                        System.err.println("Failed to send cheque bounced notification: " + e.getMessage());
                    }
                    
                    showSuccess("Cheque bounced successfully");
                    
                    bounceReasonArea.clear();
                    loadDepositedCheques();
                    loadAllCheques();
                    loadStatistics();
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
                
            } catch (SQLException e) {
                showError("Error bouncing cheque: " + e.getMessage());
            }
        }
    }
    
    private void loadAllChequeBooks() {
        allBooks.clear();
        
        String query = "SELECT * FROM vw_cheque_book_summary ORDER BY request_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                ChequeBook book = new ChequeBook();
                book.setChequeBookId(rs.getInt("cheque_book_id"));
                book.setBookNumber(rs.getString("book_number"));
                book.setAccountNumber(String.valueOf(rs.getInt("accountNumber")));
                book.setCustomerName(rs.getString("customer_name"));
                book.setStatus(rs.getString("status"));
                book.setRemainingLeaves(rs.getInt("remaining_leaves"));
                book.setApprovalDate(rs.getTimestamp("approval_date"));
                
                allBooks.add(book);
            }
            
            allBooksTable.setItems(allBooks);
            
        } catch (SQLException e) {
            showError("Error loading cheque books: " + e.getMessage());
        }
    }
    
    private void loadAllCheques() {
        allCheques.clear();
        
        String query = "SELECT * FROM vw_cheque_details ORDER BY issue_date DESC LIMIT 1000";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Cheque cheque = new Cheque();
                cheque.setChequeId(rs.getInt("cheque_id"));
                cheque.setChequeNumber(rs.getString("cheque_number"));
                cheque.setAccountNumber(String.valueOf(rs.getInt("accountNumber")));
                cheque.setAmount(rs.getDouble("amount"));
                cheque.setPayeeName(rs.getString("payee_name"));
                cheque.setStatus(rs.getString("status"));
                cheque.setClearanceDate(rs.getTimestamp("clearance_date"));
                
                allCheques.add(cheque);
            }
            
            allChequesTable.setItems(allCheques);
            
        } catch (SQLException e) {
            showError("Error loading cheques: " + e.getMessage());
        }
    }
    
    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Cheque Book Statistics
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM cheque_books");
            if (rs.next()) totalRequestsLabel.setText(String.valueOf(rs.getInt("total")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as pending FROM cheque_books WHERE status = 'PENDING'");
            if (rs.next()) pendingRequestsLabel.setText(String.valueOf(rs.getInt("pending")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as approved FROM cheque_books WHERE status IN ('APPROVED', 'ISSUED')");
            if (rs.next()) approvedBooksLabel.setText(String.valueOf(rs.getInt("approved")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as active FROM cheque_books WHERE status = 'ISSUED'");
            if (rs.next()) activeBooksLabel.setText(String.valueOf(rs.getInt("active")));
            
            // Cheque Statistics
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM cheques");
            if (rs.next()) totalChequesLabel.setText(String.valueOf(rs.getInt("total")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as cleared FROM cheques WHERE status = 'CLEARED'");
            if (rs.next()) clearedChequesLabel.setText(String.valueOf(rs.getInt("cleared")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as bounced FROM cheques WHERE status = 'BOUNCED'");
            if (rs.next()) bouncedChequesLabel.setText(String.valueOf(rs.getInt("bounced")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as pending FROM cheques WHERE status IN ('DEPOSITED', 'PENDING_CLEARANCE')");
            if (rs.next()) pendingClearanceLabel.setText(String.valueOf(rs.getInt("pending")));
            
        } catch (SQLException e) {
            showError("Error loading statistics: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSearchBooks() {
        String searchText = searchBookField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadAllChequeBooks();
            return;
        }
        
        ObservableList<ChequeBook> filtered = FXCollections.observableArrayList();
        for (ChequeBook book : allBooks) {
            if (book.getBookNumber().toLowerCase().contains(searchText) ||
                book.getAccountNumber().toLowerCase().contains(searchText) ||
                book.getCustomerName().toLowerCase().contains(searchText)) {
                filtered.add(book);
            }
        }
        allBooksTable.setItems(filtered);
    }
    
    @FXML
    private void handleSearchCheques() {
        String searchText = searchChequeField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadAllCheques();
            return;
        }
        
        ObservableList<Cheque> filtered = FXCollections.observableArrayList();
        for (Cheque cheque : allCheques) {
            if (cheque.getChequeNumber().toLowerCase().contains(searchText) ||
                cheque.getAccountNumber().toLowerCase().contains(searchText) ||
                (cheque.getPayeeName() != null && cheque.getPayeeName().toLowerCase().contains(searchText))) {
                filtered.add(cheque);
            }
        }
        allChequesTable.setItems(filtered);
    }
    
    private void filterChequeBooks() {
        String status = statusFilterCombo.getValue();
        if (status == null || status.equals("ALL")) {
            loadAllChequeBooks();
            return;
        }
        
        ObservableList<ChequeBook> filtered = FXCollections.observableArrayList();
        for (ChequeBook book : allBooks) {
            if (book.getStatus().equals(status)) {
                filtered.add(book);
            }
        }
        allBooksTable.setItems(filtered);
    }
    
    private void filterCheques() {
        String status = chequeStatusFilterCombo.getValue();
        if (status == null || status.equals("ALL")) {
            loadAllCheques();
            return;
        }
        
        ObservableList<Cheque> filtered = FXCollections.observableArrayList();
        for (Cheque cheque : allCheques) {
            if (cheque.getStatus().equals(status)) {
                filtered.add(cheque);
            }
        }
        allChequesTable.setItems(filtered);
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
    
    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StaffDashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) pendingRequestsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Staff Dashboard");
            
        } catch (IOException e) {
            showError("Error returning to dashboard: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadPendingRequests();
        loadDepositedCheques();
        loadAllChequeBooks();
        loadAllCheques();
        loadStatistics();
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
