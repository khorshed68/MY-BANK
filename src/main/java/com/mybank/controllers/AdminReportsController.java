package com.mybank.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.mybank.Main;
import com.mybank.database.DatabaseConnection;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

/**
 * Controller for Admin Reports and Analytics.
 */ 
public class AdminReportsController {
    
    @FXML
    private TableView<ReportRow> reportTable;
    
    @FXML
    private ComboBox<String> reportTypeCombo;
    
    @FXML
    private Label summaryLabel;
    
    @FXML
    private void initialize() {
        setupReportTypes();
        setupTableColumns();
    }
    
    private void setupReportTypes() {
        if (reportTypeCombo != null) {
            reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Daily Transaction Reports",
                "Weekly Activity Reports",
                "Monthly Financial Reports",
                "Account Status Reports",
                "Staff Activity Reports",
                "All Transactions",
                "User Summary"
            ));
            reportTypeCombo.getSelectionModel().selectFirst();
        }
    }
    
    private void setupTableColumns() {
        if (reportTable != null) {
            reportTable.getColumns().clear();
            // Columns will be set dynamically based on report type
        }
    }
    
    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }
    
    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeCombo != null ? reportTypeCombo.getValue() : "All Transactions";
        if (reportType == null) {
            reportType = "All Transactions";
        }
        
        try {
            switch (reportType) {
                case "Daily Transaction Reports":
                    generateDailyTransactionReport();
                    break;
                case "Account Status Reports":
                    generateAccountStatusReport();
                    break;
                case "Staff Activity Reports":
                    generateStaffActivityReport();
                    break;
                case "User Summary":
                    generateUserSummaryReport();
                    break;
                default:
                    generateAllTransactionsReport();
                    break;
            }
        } catch (Exception e) {
            showError("Error generating report: " + e.getMessage());
        }
    }
    
    private void generateAllTransactionsReport() throws SQLException {
        ObservableList<ReportRow> data = FXCollections.observableArrayList();
        
        String query = "SELECT t.id, t.accountNumber, t.type, t.amount, t.timestamp " +
                      "FROM transactions t ORDER BY t.timestamp DESC LIMIT 100";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            setupDynamicColumns("ID", "Account Number", "Type", "Amount", "Timestamp");
            
            while (rs.next()) {
                data.add(new ReportRow(
                    rs.getString("id"),
                    rs.getString("accountNumber"),
                    rs.getString("type"),
                    String.format("%.2f", rs.getDouble("amount")),
                    rs.getString("timestamp")
                ));
            }
            
            reportTable.setItems(data);
            updateSummary("Total Transactions: " + data.size());
        }
    }
    
    private void generateDailyTransactionReport() throws SQLException {
        ObservableList<ReportRow> data = FXCollections.observableArrayList();
        
        String query = "SELECT DATE(timestamp) as date, COUNT(*) as count, SUM(amount) as total " +
                      "FROM transactions WHERE DATE(timestamp) = CURRENT_DATE " +
                      "GROUP BY DATE(timestamp)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            setupDynamicColumns("Date", "Transaction Count", "Total Amount");
            
            double grandTotal = 0;
            while (rs.next()) {
                String date = rs.getString("date");
                String count = rs.getString("count");
                double total = rs.getDouble("total");
                grandTotal += total;
                
                data.add(new ReportRow(date, count, String.format("%.2f", total)));
            }
            
            reportTable.setItems(data);
            updateSummary("Today's Total: TAKA " + String.format("%.2f", grandTotal));
        }
    }
    
    private void generateAccountStatusReport() throws SQLException {
        ObservableList<ReportRow> data = FXCollections.observableArrayList();
        
        String query = "SELECT accountNumber, ownerName, accountType, balance, status " +
                      "FROM accounts ORDER BY balance DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            setupDynamicColumns("Account Number", "Owner Name", "Type", "Balance", "Status");
            
            double totalBalance = 0;
            while (rs.next()) {
                double balance = rs.getDouble("balance");
                totalBalance += balance;
                
                data.add(new ReportRow(
                    rs.getString("accountNumber"),
                    rs.getString("ownerName"),
                    rs.getString("accountType"),
                    String.format("%.2f", balance),
                    rs.getString("status")
                ));
            }
            
            reportTable.setItems(data);
            updateSummary("Total Accounts: " + data.size() + " | Total Balance: TAKA " + String.format("%.2f", totalBalance));
        }
    }
    
    private void generateStaffActivityReport() throws SQLException {
        ObservableList<ReportRow> data = FXCollections.observableArrayList();
        
        String query = "SELECT staffId, username, fullName, email, role " +
                      "FROM staff ORDER BY role, username";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            setupDynamicColumns("Staff ID", "Username", "Full Name", "Email", "Role");
            
            while (rs.next()) {
                data.add(new ReportRow(
                    rs.getString("staffId"),
                    rs.getString("username"),
                    rs.getString("fullName"),
                    rs.getString("email"),
                    rs.getString("role")
                ));
            }
            
            reportTable.setItems(data);
            updateSummary("Total Staff Members: " + data.size());
        }
    }
    
    private void generateUserSummaryReport() throws SQLException {
        ObservableList<ReportRow> data = FXCollections.observableArrayList();
        
        String query = "SELECT role, COUNT(*) as count FROM staff GROUP BY role";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            setupDynamicColumns("Role", "Count");
            
            int total = 0;
            while (rs.next()) {
                String count = rs.getString("count");
                total += Integer.parseInt(count);
                data.add(new ReportRow(rs.getString("role"), count));
            }
            
            reportTable.setItems(data);
            updateSummary("Total Staff: " + total);
        }
    }
    
    private void setupDynamicColumns(String... columnNames) {
        if (reportTable == null) return;
        
        reportTable.getColumns().clear();
        
        for (int i = 0; i < columnNames.length && i < 6; i++) {
            final int index = i;
            TableColumn<ReportRow, String> column = new TableColumn<>(columnNames[i]);
            column.setCellValueFactory(cellData -> cellData.getValue().getColumn(index));
            column.setPrefWidth(150);
            reportTable.getColumns().add(column);
        }
    }
    
    private void updateSummary(String text) {
        if (summaryLabel != null) {
            summaryLabel.setText(text);
        }
    }
    
    @FXML
    private void handleExportPDF() {
        showInfo("PDF Export", "PDF export functionality would require additional libraries like iText or Apache PDFBox.\n\nFor now, please use CSV export.");
    }
    
    @FXML
    private void handleExportCSV() {
        if (reportTable == null || reportTable.getItems().isEmpty()) {
            showError("No data to export. Please generate a report first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as CSV");
        fileChooser.setInitialFileName("report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToCSV(file);
                showSuccess("Report exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Error exporting to CSV: " + e.getMessage());
            }
        }
    }
    
    private void exportToCSV(File file) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header
            for (int i = 0; i < reportTable.getColumns().size(); i++) {
                if (i > 0) writer.write(",");
                writer.write(reportTable.getColumns().get(i).getText());
            }
            writer.newLine();
            
            // Write data
            for (ReportRow row : reportTable.getItems()) {
                for (int i = 0; i < reportTable.getColumns().size(); i++) {
                    if (i > 0) writer.write(",");
                    String value = row.getColumn(i).getValue();
                    // Escape quotes and commas
                    if (value.contains(",") || value.contains("\"")) {
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }
                    writer.write(value);
                }
                writer.newLine();
            }
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Reports & Analytics");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Export Successful");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Report Row data model
    public static class ReportRow {
        private final SimpleStringProperty col1;
        private final SimpleStringProperty col2;
        private final SimpleStringProperty col3;
        private final SimpleStringProperty col4;
        private final SimpleStringProperty col5;
        private final SimpleStringProperty col6;
        
        public ReportRow(String col1, String col2) {
            this(col1, col2, "", "", "", "");
        }
        
        public ReportRow(String col1, String col2, String col3) {
            this(col1, col2, col3, "", "", "");
        }
        
        public ReportRow(String col1, String col2, String col3, String col4, String col5) {
            this(col1, col2, col3, col4, col5, "");
        }
        
        public ReportRow(String col1, String col2, String col3, String col4, String col5, String col6) {
            this.col1 = new SimpleStringProperty(col1 != null ? col1 : "");
            this.col2 = new SimpleStringProperty(col2 != null ? col2 : "");
            this.col3 = new SimpleStringProperty(col3 != null ? col3 : "");
            this.col4 = new SimpleStringProperty(col4 != null ? col4 : "");
            this.col5 = new SimpleStringProperty(col5 != null ? col5 : "");
            this.col6 = new SimpleStringProperty(col6 != null ? col6 : "");
        }
        
        public SimpleStringProperty getColumn(int index) {
            switch (index) {
                case 0: return col1;
                case 1: return col2;
                case 2: return col3;
                case 3: return col4;
                case 4: return col5;
                case 5: return col6;
                default: return new SimpleStringProperty("");
            }
        }
    }
}
