package com.mybank.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;

import com.mybank.database.DatabaseConnection;
import com.mybank.models.Cheque;
import com.mybank.models.ChequeBook;
import com.mybank.models.ChequeTransaction;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AdminChequeOversightController {
    
    // Overview Statistics
    @FXML private Label totalBooksLabel;
    @FXML private Label pendingBooksLabel;
    @FXML private Label activeBooksLabel;
    @FXML private Label totalChequesLabel;
    @FXML private Label clearedChequesLabel;
    @FXML private Label bouncedChequesLabel;
    @FXML private Label totalAmountClearedLabel;
    @FXML private Label totalAmountBouncedLabel;
    
    // All Cheque Books Table
    @FXML private TableView<ChequeBook> allBooksTable;
    @FXML private TableColumn<ChequeBook, String> bookNumberCol;
    @FXML private TableColumn<ChequeBook, String> accountCol;
    @FXML private TableColumn<ChequeBook, String> customerCol;
    @FXML private TableColumn<ChequeBook, String> statusCol;
    @FXML private TableColumn<ChequeBook, Integer> totalLeavesCol;
    @FXML private TableColumn<ChequeBook, Integer> remainingLeavesCol;
    @FXML private TableColumn<ChequeBook, Timestamp> requestDateCol;
    @FXML private TableColumn<ChequeBook, String> approvedByCol;
    
    @FXML private TextField searchBookField;
    @FXML private ComboBox<String> bookStatusFilter;
    @FXML private DatePicker bookFromDatePicker;
    @FXML private DatePicker bookToDatePicker;
    
    // All Cheques Table
    @FXML private TableView<Cheque> allChequesTable;
    @FXML private TableColumn<Cheque, String> chequeNumberCol;
    @FXML private TableColumn<Cheque, String> chequeAccountCol;
    @FXML private TableColumn<Cheque, String> chequeCustomerCol;
    @FXML private TableColumn<Cheque, Double> chequeAmountCol;
    @FXML private TableColumn<Cheque, String> chequePayeeCol;
    @FXML private TableColumn<Cheque, String> chequeStatusCol;
    @FXML private TableColumn<Cheque, Date> chequeIssueDateCol;
    @FXML private TableColumn<Cheque, Timestamp> chequeClearanceDateCol;
    @FXML private TableColumn<Cheque, String> bouncedReasonCol;
    
    @FXML private TextField searchChequeField;
    @FXML private ComboBox<String> chequeStatusFilter;
    @FXML private DatePicker chequeFromDatePicker;
    @FXML private DatePicker chequeToDatePicker;
    
    // Transaction History Table
    @FXML private TableView<ChequeTransaction> transactionTable;
    @FXML private TableColumn<ChequeTransaction, String> txChequeNumCol;
    @FXML private TableColumn<ChequeTransaction, String> txAccountCol;
    @FXML private TableColumn<ChequeTransaction, String> txTypeCol;
    @FXML private TableColumn<ChequeTransaction, String> txOldStatusCol;
    @FXML private TableColumn<ChequeTransaction, String> txNewStatusCol;
    @FXML private TableColumn<ChequeTransaction, Double> txAmountCol;
    @FXML private TableColumn<ChequeTransaction, Timestamp> txDateCol;
    @FXML private TableColumn<ChequeTransaction, String> txPerformedByCol;
    @FXML private TableColumn<ChequeTransaction, String> txUserTypeCol;
    
    @FXML private DatePicker txFromDatePicker;
    @FXML private DatePicker txToDatePicker;
    @FXML private ComboBox<String> txTypeFilter;
    
    // Eligibility Settings
    @FXML private ComboBox<String> accountTypeCombo;
    @FXML private TextField minBalanceField;
    @FXML private TextField minAccountAgeField;
    @FXML private TextField maxBooksPerYearField;
    @FXML private TextField leavesPerBookField;
    @FXML private CheckBox isActiveCheck;
    @FXML private Button saveEligibilityBtn;
    @FXML private Button deleteEligibilityBtn;
    
    @FXML private TableView<EligibilityCriteria> eligibilityTable;
    @FXML private TableColumn<EligibilityCriteria, String> eligAccountTypeCol;
    @FXML private TableColumn<EligibilityCriteria, Double> eligMinBalanceCol;
    @FXML private TableColumn<EligibilityCriteria, Integer> eligMinAgeCol;
    @FXML private TableColumn<EligibilityCriteria, Integer> eligMaxBooksCol;
    @FXML private TableColumn<EligibilityCriteria, Integer> eligLeavesCol;
    @FXML private TableColumn<EligibilityCriteria, Boolean> eligActiveCol;
    
    private int adminId;
    private ObservableList<ChequeBook> allBooks = FXCollections.observableArrayList();
    private ObservableList<Cheque> allCheques = FXCollections.observableArrayList();
    private ObservableList<ChequeTransaction> transactions = FXCollections.observableArrayList();
    private ObservableList<EligibilityCriteria> eligibilityCriteria = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        adminId = SessionManager.getCurrentAdminId();
        
        setupTables();
        setupFilters();
        loadStatistics();
        loadAllChequeBooks();
        loadAllCheques();
        loadTransactionHistory();
        loadEligibilityCriteria();
    }
    
    private void setupTables() {
        // Books Table
        bookNumberCol.setCellValueFactory(new PropertyValueFactory<>("bookNumber"));
        accountCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalLeavesCol.setCellValueFactory(new PropertyValueFactory<>("totalLeaves"));
        remainingLeavesCol.setCellValueFactory(new PropertyValueFactory<>("remainingLeaves"));
        requestDateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        approvedByCol.setCellValueFactory(new PropertyValueFactory<>("approvedByName"));
        
        // Cheques Table
        chequeNumberCol.setCellValueFactory(new PropertyValueFactory<>("chequeNumber"));
        chequeAccountCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        chequeCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        chequeAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        chequePayeeCol.setCellValueFactory(new PropertyValueFactory<>("payeeName"));
        chequeStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        chequeIssueDateCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        chequeClearanceDateCol.setCellValueFactory(new PropertyValueFactory<>("clearanceDate"));
        bouncedReasonCol.setCellValueFactory(new PropertyValueFactory<>("bounceReason"));
        
        // Transaction Table
        txChequeNumCol.setCellValueFactory(new PropertyValueFactory<>("chequeNumber"));
        txAccountCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        txTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        txOldStatusCol.setCellValueFactory(new PropertyValueFactory<>("oldStatus"));
        txNewStatusCol.setCellValueFactory(new PropertyValueFactory<>("newStatus"));
        txAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        txDateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        txPerformedByCol.setCellValueFactory(new PropertyValueFactory<>("performedByName"));
        txUserTypeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        
        // Eligibility Table
        eligAccountTypeCol.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        eligMinBalanceCol.setCellValueFactory(new PropertyValueFactory<>("minimumBalance"));
        eligMinAgeCol.setCellValueFactory(new PropertyValueFactory<>("minimumAccountAgeDays"));
        eligMaxBooksCol.setCellValueFactory(new PropertyValueFactory<>("maxBooksPerYear"));
        eligLeavesCol.setCellValueFactory(new PropertyValueFactory<>("leavesPerBook"));
        eligActiveCol.setCellValueFactory(new PropertyValueFactory<>("isActive"));
    }
    
    private void setupFilters() {
        bookStatusFilter.setItems(FXCollections.observableArrayList(
            "ALL", "PENDING", "APPROVED", "ISSUED", "COMPLETED", "REJECTED", "CANCELLED"
        ));
        bookStatusFilter.setValue("ALL");
        
        chequeStatusFilter.setItems(FXCollections.observableArrayList(
            "ALL", "ISSUED", "DEPOSITED", "PENDING_CLEARANCE", "CLEARED", "BOUNCED", "CANCELLED", "VOID"
        ));
        chequeStatusFilter.setValue("ALL");
        
        txTypeFilter.setItems(FXCollections.observableArrayList(
            "ALL", "ISSUE", "DEPOSIT", "CLEAR", "BOUNCE", "CANCEL", "VOID"
        ));
        txTypeFilter.setValue("ALL");
        
        accountTypeCombo.setItems(FXCollections.observableArrayList(
            "SAVINGS", "CURRENT", "SALARY", "FIXED_DEPOSIT", "RECURRING"
        ));
    }
    
    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Cheque Book Statistics
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM cheque_books");
            if (rs.next()) totalBooksLabel.setText(String.valueOf(rs.getInt("total")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as pending FROM cheque_books WHERE status = 'PENDING'");
            if (rs.next()) pendingBooksLabel.setText(String.valueOf(rs.getInt("pending")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as active FROM cheque_books WHERE status = 'ISSUED'");
            if (rs.next()) activeBooksLabel.setText(String.valueOf(rs.getInt("active")));
            
            // Cheque Statistics
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM cheques");
            if (rs.next()) totalChequesLabel.setText(String.valueOf(rs.getInt("total")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as cleared FROM cheques WHERE status = 'CLEARED'");
            if (rs.next()) clearedChequesLabel.setText(String.valueOf(rs.getInt("cleared")));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as bounced FROM cheques WHERE status = 'BOUNCED'");
            if (rs.next()) bouncedChequesLabel.setText(String.valueOf(rs.getInt("bounced")));
            
            // Amount Statistics
            rs = stmt.executeQuery("SELECT COALESCE(SUM(amount), 0) as total FROM cheques WHERE status = 'CLEARED'");
            if (rs.next()) totalAmountClearedLabel.setText(String.format("TAKA %.2f", rs.getDouble("total")));
            
            rs = stmt.executeQuery("SELECT COALESCE(SUM(amount), 0) as total FROM cheques WHERE status = 'BOUNCED'");
            if (rs.next()) totalAmountBouncedLabel.setText(String.format("TAKA %.2f", rs.getDouble("total")));
            
        } catch (SQLException e) {
            showError("Error loading statistics: " + e.getMessage());
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
                book.setTotalLeaves(rs.getInt("total_leaves"));
                book.setRemainingLeaves(rs.getInt("remaining_leaves"));
                book.setRequestDate(rs.getTimestamp("request_date"));
                book.setApprovalDate(rs.getTimestamp("approval_date"));
                book.setApprovedByName(rs.getString("approved_by_name"));
                
                allBooks.add(book);
            }
            
            allBooksTable.setItems(allBooks);
            
        } catch (SQLException e) {
            showError("Error loading cheque books: " + e.getMessage());
        }
    }
    
    private void loadAllCheques() {
        allCheques.clear();
        
        String query = "SELECT * FROM vw_cheque_details ORDER BY issue_date DESC LIMIT 5000";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Cheque cheque = new Cheque();
                cheque.setChequeId(rs.getInt("cheque_id"));
                cheque.setChequeNumber(rs.getString("cheque_number"));
                cheque.setAccountNumber(String.valueOf(rs.getInt("accountNumber")));
                cheque.setCustomerName(rs.getString("account_holder"));
                cheque.setAmount(rs.getDouble("amount"));
                cheque.setPayeeName(rs.getString("payee_name"));
                cheque.setStatus(rs.getString("status"));
                cheque.setIssueDate(rs.getDate("issue_date"));
                cheque.setClearanceDate(rs.getTimestamp("clearance_date"));
                cheque.setBounceReason(rs.getString("bounce_reason"));
                
                allCheques.add(cheque);
            }
            
            allChequesTable.setItems(allCheques);
            
        } catch (SQLException e) {
            showError("Error loading cheques: " + e.getMessage());
        }
    }
    
    private void loadTransactionHistory() {
        transactions.clear();
        
        String query = "SELECT * FROM vw_cheque_transaction_history ORDER BY transaction_date DESC LIMIT 1000";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                ChequeTransaction tx = new ChequeTransaction();
                tx.setTransactionId(rs.getInt("transaction_id"));
                tx.setChequeNumber(rs.getString("cheque_number"));
                tx.setAccountNumber(String.valueOf(rs.getInt("accountNumber")));
                tx.setTransactionType(rs.getString("transaction_type"));
                tx.setOldStatus(rs.getString("old_status"));
                tx.setNewStatus(rs.getString("new_status"));
                tx.setAmount(rs.getDouble("amount"));
                tx.setTransactionDate(rs.getTimestamp("transaction_date"));
                tx.setPerformedByName(rs.getString("performed_by_name"));
                tx.setUserType(rs.getString("user_type"));
                
                transactions.add(tx);
            }
            
            transactionTable.setItems(transactions);
            
        } catch (SQLException e) {
            showError("Error loading transaction history: " + e.getMessage());
        }
    }
    
    private void loadEligibilityCriteria() {
        eligibilityCriteria.clear();
        
        String query = "SELECT * FROM cheque_book_eligibility ORDER BY account_type";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                EligibilityCriteria criteria = new EligibilityCriteria();
                criteria.setId(rs.getInt("id"));
                criteria.setAccountType(rs.getString("account_type"));
                criteria.setMinimumBalance(rs.getDouble("minimum_balance"));
                criteria.setMinimumAccountAgeDays(rs.getInt("minimum_account_age_days"));
                criteria.setMaxBooksPerYear(rs.getInt("max_books_per_year"));
                criteria.setLeavesPerBook(rs.getInt("leaves_per_book"));
                criteria.setIsActive(rs.getBoolean("is_active"));
                
                eligibilityCriteria.add(criteria);
            }
            
            eligibilityTable.setItems(eligibilityCriteria);
            
        } catch (SQLException e) {
            showError("Error loading eligibility criteria: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSearchBooks() {
        String searchText = searchBookField.getText().trim().toLowerCase();
        String status = bookStatusFilter.getValue();
        LocalDate fromDate = bookFromDatePicker.getValue();
        LocalDate toDate = bookToDatePicker.getValue();
        
        ObservableList<ChequeBook> filtered = FXCollections.observableArrayList();
        
        for (ChequeBook book : allBooks) {
            boolean matches = true;
            
            if (!searchText.isEmpty()) {
                matches = book.getBookNumber().toLowerCase().contains(searchText) ||
                         book.getAccountNumber().toLowerCase().contains(searchText) ||
                         book.getCustomerName().toLowerCase().contains(searchText);
            }
            
            if (matches && status != null && !status.equals("ALL")) {
                matches = book.getStatus().equals(status);
            }
            
            if (matches && fromDate != null && book.getRequestDate() != null) {
                matches = book.getRequestDate().toLocalDateTime().toLocalDate().isAfter(fromDate.minusDays(1));
            }
            
            if (matches && toDate != null && book.getRequestDate() != null) {
                matches = book.getRequestDate().toLocalDateTime().toLocalDate().isBefore(toDate.plusDays(1));
            }
            
            if (matches) {
                filtered.add(book);
            }
        }
        
        allBooksTable.setItems(filtered);
    }
    
    @FXML
    private void handleSearchCheques() {
        String searchText = searchChequeField.getText().trim().toLowerCase();
        String status = chequeStatusFilter.getValue();
        LocalDate fromDate = chequeFromDatePicker.getValue();
        LocalDate toDate = chequeToDatePicker.getValue();
        
        ObservableList<Cheque> filtered = FXCollections.observableArrayList();
        
        for (Cheque cheque : allCheques) {
            boolean matches = true;
            
            if (!searchText.isEmpty()) {
                matches = cheque.getChequeNumber().toLowerCase().contains(searchText) ||
                         cheque.getAccountNumber().toLowerCase().contains(searchText) ||
                         (cheque.getCustomerName() != null && cheque.getCustomerName().toLowerCase().contains(searchText)) ||
                         (cheque.getPayeeName() != null && cheque.getPayeeName().toLowerCase().contains(searchText));
            }
            
            if (matches && status != null && !status.equals("ALL")) {
                matches = cheque.getStatus().equals(status);
            }
            
            if (matches && fromDate != null && cheque.getIssueDate() != null) {
                matches = cheque.getIssueDate().toLocalDate().isAfter(fromDate.minusDays(1));
            }
            
            if (matches && toDate != null && cheque.getIssueDate() != null) {
                matches = cheque.getIssueDate().toLocalDate().isBefore(toDate.plusDays(1));
            }
            
            if (matches) {
                filtered.add(cheque);
            }
        }
        
        allChequesTable.setItems(filtered);
    }
    
    @FXML
    private void handleFilterTransactions() {
        String txType = txTypeFilter.getValue();
        LocalDate fromDate = txFromDatePicker.getValue();
        LocalDate toDate = txToDatePicker.getValue();
        
        ObservableList<ChequeTransaction> filtered = FXCollections.observableArrayList();
        
        for (ChequeTransaction tx : transactions) {
            boolean matches = true;
            
            if (txType != null && !txType.equals("ALL")) {
                matches = tx.getTransactionType().equals(txType);
            }
            
            if (matches && fromDate != null && tx.getTransactionDate() != null) {
                matches = tx.getTransactionDate().toLocalDateTime().toLocalDate().isAfter(fromDate.minusDays(1));
            }
            
            if (matches && toDate != null && tx.getTransactionDate() != null) {
                matches = tx.getTransactionDate().toLocalDateTime().toLocalDate().isBefore(toDate.plusDays(1));
            }
            
            if (matches) {
                filtered.add(tx);
            }
        }
        
        transactionTable.setItems(filtered);
    }
    
    @FXML
    private void handleSaveEligibility() {
        String accountType = accountTypeCombo.getValue();
        String minBalanceStr = minBalanceField.getText().trim();
        String minAgeStr = minAccountAgeField.getText().trim();
        String maxBooksStr = maxBooksPerYearField.getText().trim();
        String leavesStr = leavesPerBookField.getText().trim();
        
        if (accountType == null || accountType.isEmpty()) {
            showError("Please select an account type");
            return;
        }
        
        try {
            double minBalance = Double.parseDouble(minBalanceStr);
            int minAge = Integer.parseInt(minAgeStr);
            int maxBooks = Integer.parseInt(maxBooksStr);
            int leaves = Integer.parseInt(leavesStr);
            boolean isActive = isActiveCheck.isSelected();
            
            String query = "INSERT INTO cheque_book_eligibility (account_type, minimum_balance, " +
                          "minimum_account_age_days, max_books_per_year, leaves_per_book, is_active) " +
                          "VALUES (?, ?, ?, ?, ?, ?) " +
                          "ON CONFLICT(account_type) DO UPDATE SET " +
                          "minimum_balance = ?, minimum_account_age_days = ?, " +
                          "max_books_per_year = ?, leaves_per_book = ?, is_active = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setString(1, accountType);
                pstmt.setDouble(2, minBalance);
                pstmt.setInt(3, minAge);
                pstmt.setInt(4, maxBooks);
                pstmt.setInt(5, leaves);
                pstmt.setInt(6, isActive ? 1 : 0);
                pstmt.setDouble(7, minBalance);
                pstmt.setInt(8, minAge);
                pstmt.setInt(9, maxBooks);
                pstmt.setInt(10, leaves);
                pstmt.setInt(11, isActive ? 1 : 0);
                
                pstmt.executeUpdate();
                
                showSuccess("Eligibility criteria saved successfully");
                clearEligibilityForm();
                loadEligibilityCriteria();
                
            } catch (SQLException e) {
                showError("Error saving eligibility criteria: " + e.getMessage());
            }
            
        } catch (NumberFormatException e) {
            showError("Please enter valid numeric values");
        }
    }
    
    @FXML
    private void handleDeleteEligibility() {
        EligibilityCriteria selected = eligibilityTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an eligibility criteria to delete");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Eligibility Criteria");
        confirm.setContentText("Account Type: " + selected.getAccountType() + 
                              "\n\nAre you sure you want to delete this criteria?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            String query = "DELETE FROM cheque_book_eligibility WHERE id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                
                showSuccess("Eligibility criteria deleted successfully");
                loadEligibilityCriteria();
                
            } catch (SQLException e) {
                showError("Error deleting eligibility criteria: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleLoadEligibility() {
        EligibilityCriteria selected = eligibilityTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        accountTypeCombo.setValue(selected.getAccountType());
        minBalanceField.setText(String.valueOf(selected.getMinimumBalance()));
        minAccountAgeField.setText(String.valueOf(selected.getMinimumAccountAgeDays()));
        maxBooksPerYearField.setText(String.valueOf(selected.getMaxBooksPerYear()));
        leavesPerBookField.setText(String.valueOf(selected.getLeavesPerBook()));
        isActiveCheck.setSelected(selected.getIsActive());
    }
    
    private void clearEligibilityForm() {
        accountTypeCombo.setValue(null);
        minBalanceField.clear();
        minAccountAgeField.clear();
        maxBooksPerYearField.clear();
        leavesPerBookField.clear();
        isActiveCheck.setSelected(true);
    }
    
    @FXML
    private void handleExportReport() {
        showInfo("Report export functionality coming soon!");
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) allBooksTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            
        } catch (IOException e) {
            showError("Error returning to dashboard: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadStatistics();
        loadAllChequeBooks();
        loadAllCheques();
        loadTransactionHistory();
        loadEligibilityCriteria();
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
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for Eligibility Criteria
    public static class EligibilityCriteria {
        private int id;
        private String accountType;
        private double minimumBalance;
        private int minimumAccountAgeDays;
        private int maxBooksPerYear;
        private int leavesPerBook;
        private boolean isActive;
        
        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        
        public double getMinimumBalance() { return minimumBalance; }
        public void setMinimumBalance(double minimumBalance) { this.minimumBalance = minimumBalance; }
        
        public int getMinimumAccountAgeDays() { return minimumAccountAgeDays; }
        public void setMinimumAccountAgeDays(int minimumAccountAgeDays) { 
            this.minimumAccountAgeDays = minimumAccountAgeDays; 
        }
        
        public int getMaxBooksPerYear() { return maxBooksPerYear; }
        public void setMaxBooksPerYear(int maxBooksPerYear) { this.maxBooksPerYear = maxBooksPerYear; }
        
        public int getLeavesPerBook() { return leavesPerBook; }
        public void setLeavesPerBook(int leavesPerBook) { this.leavesPerBook = leavesPerBook; }
        
        public boolean getIsActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }
    }
}
