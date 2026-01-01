package com.mybank.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.mybank.Main;
import com.mybank.database.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controller for Report Generation screen.
 */
public class ReportGenerationController {
    
    @FXML
    private ComboBox<String> reportTypeComboBox;
    
    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    @FXML
    private Label messageLabel;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Populate report types
        reportTypeComboBox.getItems().addAll(
            "Transaction Summary Report",
            "Account Activity Report",
            "Customer Statistics Report",
            "Pending Approvals Report",
            "Daily Summary Report"
        );
        
        // Set default dates
        toDatePicker.setValue(LocalDate.now());
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        
        hideMessage();
    }
    
    /**
     * Handle generate report
     */
    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeComboBox.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        
        // Validation
        if (reportType == null || reportType.isEmpty()) {
            showError("Please select a report type");
            return;
        }
        
        if (fromDate == null) {
            showError("Please select a start date");
            return;
        }
        
        if (toDate == null) {
            showError("Please select an end date");
            return;
        }
        
        if (fromDate.isAfter(toDate)) {
            showError("Start date cannot be after end date");
            return;
        }
        
        // Generate and display report
        try {
            String reportContent = generateReport(reportType, fromDate, toDate);
            displayReport(reportType, reportContent);
            showSuccess("Report generated successfully!");
        } catch (Exception e) {
            showError("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate report based on type and date range
     */
    private String generateReport(String reportType, LocalDate fromDate, LocalDate toDate) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("                   MY BANK - REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append("Report Type: ").append(reportType).append("\n");
        report.append("Date Range: ").append(fromDate.format(formatter))
              .append(" to ").append(toDate.format(formatter)).append("\n");
        report.append("Generated: ").append(LocalDate.now().format(formatter)).append("\n\n");
        report.append("───────────────────────────────────────────────────────────\n\n");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            switch (reportType) {
                case "Transaction Summary Report":
                    report.append(generateTransactionSummary(conn, fromDate, toDate));
                    break;
                case "Account Activity Report":
                    report.append(generateAccountActivity(conn, fromDate, toDate));
                    break;
                case "Customer Statistics Report":
                    report.append(generateCustomerStatistics(conn, fromDate, toDate));
                    break;
                case "Pending Approvals Report":
                    report.append(generatePendingApprovals(conn));
                    break;
                case "Daily Summary Report":
                    report.append(generateDailySummary(conn, fromDate, toDate));
                    break;
                default:
                    report.append("Report type not implemented yet.\n");
            }
        } catch (SQLException e) {
            report.append("\nError accessing database: ").append(e.getMessage());
        }
        
        report.append("\n───────────────────────────────────────────────────────────\n");
        report.append("                    END OF REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
    
    /**
     * Generate transaction summary
     */
    private String generateTransactionSummary(Connection conn, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("TRANSACTION SUMMARY\n\n");
        
        try {
            // Get transaction counts and totals
            String sql = "SELECT type, COUNT(*) as count, SUM(amount) as total " +
                        "FROM transactions " +
                        "WHERE DATE(timestamp) BETWEEN ? AND ? " +
                        "GROUP BY type";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            ResultSet rs = pstmt.executeQuery();
            
            sb.append(String.format("%-20s %10s %15s\n", "Type", "Count", "Total Amount"));
            sb.append("─────────────────────────────────────────────────────\n");
            
            double grandTotal = 0;
            int totalCount = 0;
            
            while (rs.next()) {
                String type = rs.getString("type");
                int count = rs.getInt("count");
                double total = rs.getDouble("total");
                
                sb.append(String.format("%-20s %10d %15.2f\n", type, count, total));
                grandTotal += total;
                totalCount += count;
            }
            
            sb.append("─────────────────────────────────────────────────────\n");
            sb.append(String.format("%-20s %10d %15.2f\n", "TOTAL", totalCount, grandTotal));
            
        } catch (SQLException e) {
            sb.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Generate account activity report
     */
    private String generateAccountActivity(Connection conn, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("ACCOUNT ACTIVITY\n\n");
        
        try {
            String sql = "SELECT accountNumber, ownerName, " +
                        "(SELECT COUNT(*) FROM transactions WHERE accountNumber = accounts.accountNumber " +
                        "AND DATE(timestamp) BETWEEN ? AND ?) as transactionCount " +
                        "FROM accounts ORDER BY transactionCount DESC LIMIT 20";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            ResultSet rs = pstmt.executeQuery();
            
            sb.append(String.format("%-15s %-30s %15s\n", "Account #", "Owner", "Transactions"));
            sb.append("─────────────────────────────────────────────────────────────\n");
            
            while (rs.next()) {
                sb.append(String.format("%-15d %-30s %15d\n",
                    rs.getInt("accountNumber"),
                    rs.getString("ownerName"),
                    rs.getInt("transactionCount")));
            }
            
        } catch (SQLException e) {
            sb.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Generate customer statistics
     */
    private String generateCustomerStatistics(Connection conn, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("CUSTOMER STATISTICS\n\n");
        
        try {
            // Total accounts
            String sql1 = "SELECT COUNT(*) as total FROM accounts";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);
            ResultSet rs1 = pstmt1.executeQuery();
            if (rs1.next()) {
                sb.append("Total Accounts: ").append(rs1.getInt("total")).append("\n");
            }
            
            // New accounts in date range
            String sql2 = "SELECT COUNT(*) as total FROM accounts WHERE DATE(createdDate) BETWEEN ? AND ?";
            PreparedStatement pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setString(1, fromDate.toString());
            pstmt2.setString(2, toDate.toString());
            ResultSet rs2 = pstmt2.executeQuery();
            if (rs2.next()) {
                sb.append("New Accounts (in range): ").append(rs2.getInt("total")).append("\n");
            }
            
            // Average balance
            String sql3 = "SELECT AVG(balance) as avg FROM accounts";
            PreparedStatement pstmt3 = conn.prepareStatement(sql3);
            ResultSet rs3 = pstmt3.executeQuery();
            if (rs3.next()) {
                sb.append(String.format("Average Balance: ৳%.2f\n", rs3.getDouble("avg")));
            }
            
            // Total balance
            String sql4 = "SELECT SUM(balance) as total FROM accounts";
            PreparedStatement pstmt4 = conn.prepareStatement(sql4);
            ResultSet rs4 = pstmt4.executeQuery();
            if (rs4.next()) {
                sb.append(String.format("Total Deposits: ৳%.2f\n", rs4.getDouble("total")));
            }
            
        } catch (SQLException e) {
            sb.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Generate pending approvals report
     */
    private String generatePendingApprovals(Connection conn) {
        StringBuilder sb = new StringBuilder();
        sb.append("PENDING ACCOUNT APPROVALS\n\n");
        
        try {
            String sql = "SELECT requestId, customerName, accountType, initialDeposit, requestDate " +
                        "FROM account_requests WHERE status = 'PENDING' ORDER BY requestDate";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            sb.append(String.format("%-10s %-25s %-15s %-15s %s\n", 
                "Request#", "Customer", "Type", "Deposit", "Date"));
            sb.append("─────────────────────────────────────────────────────────────────────────\n");
            
            int count = 0;
            while (rs.next()) {
                sb.append(String.format("%-10d %-25s %-15s ৳%-14.2f %s\n",
                    rs.getInt("requestId"),
                    rs.getString("customerName"),
                    rs.getString("accountType"),
                    rs.getDouble("initialDeposit"),
                    rs.getString("requestDate").substring(0, 10)));
                count++;
            }
            
            sb.append("\nTotal Pending: ").append(count).append("\n");
            
        } catch (SQLException e) {
            sb.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Generate daily summary
     */
    private String generateDailySummary(Connection conn, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("DAILY SUMMARY\n\n");
        
        try {
            String sql = "SELECT DATE(timestamp) as date, " +
                        "COUNT(*) as transactions, " +
                        "SUM(amount) as total " +
                        "FROM transactions " +
                        "WHERE DATE(timestamp) BETWEEN ? AND ? " +
                        "GROUP BY DATE(timestamp) ORDER BY date DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fromDate.toString());
            pstmt.setString(2, toDate.toString());
            ResultSet rs = pstmt.executeQuery();
            
            sb.append(String.format("%-15s %15s %20s\n", "Date", "Transactions", "Total Amount"));
            sb.append("────────────────────────────────────────────────────────\n");
            
            while (rs.next()) {
                sb.append(String.format("%-15s %15d %20.2f\n",
                    rs.getString("date"),
                    rs.getInt("transactions"),
                    rs.getDouble("total")));
            }
            
        } catch (SQLException e) {
            sb.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Display report in a dialog
     */
    private void displayReport(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Generated");
        alert.setHeaderText(title);
        
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefWidth(800);
        textArea.setPrefHeight(600);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(850, 650);
        
        alert.showAndWait();
    }
    
    /**
     * Handle back to dashboard
     */
    @FXML
    private void handleBack() {
        Main.showStaffDashboard();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    /**
     * Hide message
     */
    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
}
