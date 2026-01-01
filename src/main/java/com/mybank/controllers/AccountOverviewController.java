package com.mybank.controllers;

import com.mybank.Main;
import com.mybank.models.AccountOverview;
import com.mybank.models.LoanAccount;
import com.mybank.services.AccountOverviewService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Account Overview Controller
 * Displays comprehensive account information including account details,
 * balances, status, and loan information if applicable
 */
public class AccountOverviewController {
    
    // Account Information Labels
    @FXML private Label accountNumberLabel;
    @FXML private Label ownerNameLabel;
    @FXML private Label accountTypeLabel;
    @FXML private Label accountStatusLabel;
    @FXML private Label createdDateLabel;
    
    // Balance Information Labels
    @FXML private Label currentBalanceLabel;
    @FXML private Label availableBalanceLabel;
    @FXML private Label lastTransactionLabel;
    
    // Statistics Labels
    @FXML private Label totalTransactionsLabel;
    @FXML private Label totalDepositsLabel;
    @FXML private Label totalWithdrawalsLabel;
    @FXML private Label accountHealthLabel;
    
    // Loan Information Section (visible only for loan accounts)
    @FXML private VBox loanInfoSection;
    @FXML private Label loanAmountLabel;
    @FXML private Label outstandingBalanceLabel;
    @FXML private Label interestRateLabel;
    @FXML private Label installmentAmountLabel;
    @FXML private Label nextDueDateLabel;
    @FXML private Label repaymentStatusLabel;
    @FXML private Label loanTermLabel;
    @FXML private Label repaymentProgressLabel;
    
    // Contact Information Labels
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    
    @FXML private Label messageLabel;
    
    private AccountOverviewService overviewService;
    private int loggedInAccountNumber;
    
    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        overviewService = new AccountOverviewService();
        loggedInAccountNumber = Main.getLoggedInAccount();
        
        if (loggedInAccountNumber > 0) {
            loadAccountOverview();
        } else {
            showError("Error: No user logged in!");
        }
    }
    
    /**
     * Loads and displays complete account overview
     */
    private void loadAccountOverview() {
        try {
            // Get account overview
            AccountOverview account = overviewService.getAccountOverview(
                loggedInAccountNumber, loggedInAccountNumber
            );
            
            if (account == null) {
                showError("Unable to load account information.");
                return;
            }
            
            // Display account information
            displayAccountInfo(account);
            
            // Display balance information
            displayBalanceInfo(account);
            
            // Display statistics
            displayStatistics();
            
            // Display contact information
            displayContactInfo(account);
            
            // Check if this is a loan account and display loan info
            if ("LOAN".equals(account.getAccountType())) {
                displayLoanInfo();
            } else {
                // Hide loan section for non-loan accounts
                if (loanInfoSection != null) {
                    loanInfoSection.setVisible(false);
                    loanInfoSection.setManaged(false);
                }
            }
            
            showSuccess("Account overview loaded successfully!");
            
        } catch (Exception e) {
            showError("Error loading account overview: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Displays account basic information
     */
    private void displayAccountInfo(AccountOverview account) {
        accountNumberLabel.setText(account.getMaskedAccountNumber());
        ownerNameLabel.setText(account.getOwnerName());
        accountTypeLabel.setText(account.getAccountTypeDisplay());
        
        // Set status with color coding
        accountStatusLabel.setText(account.getStatusDisplay());
        if (account.isActive()) {
            accountStatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        } else if (account.isBlocked()) {
            accountStatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        } else {
            accountStatusLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
        }
        
        createdDateLabel.setText(account.getCreatedDate());
    }
    
    /**
     * Displays balance information
     */
    private void displayBalanceInfo(AccountOverview account) {
        currentBalanceLabel.setText(account.getFormattedBalance());
        currentBalanceLabel.setStyle("-fx-text-fill: #1565c0; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        availableBalanceLabel.setText(account.getFormattedAvailableBalance());
        availableBalanceLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        lastTransactionLabel.setText(account.getLastTransactionDate());
    }
    
    /**
     * Displays account statistics
     */
    private void displayStatistics() {
        double[] stats = overviewService.getAccountStatistics(
            loggedInAccountNumber, loggedInAccountNumber
        );
        
        totalTransactionsLabel.setText(String.format("%.0f", stats[0]));
        totalDepositsLabel.setText(String.format("%.2f TAKA", stats[1]));
        totalWithdrawalsLabel.setText(String.format("%.2f TAKA", stats[2]));
        
        // Display account health
        AccountOverview account = overviewService.getAccountOverview(
            loggedInAccountNumber, loggedInAccountNumber
        );
        String health = overviewService.getAccountHealth(account);
        accountHealthLabel.setText(health);
        
        switch (health) {
            case "HEALTHY":
                accountHealthLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                break;
            case "WARNING":
                accountHealthLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
                break;
            case "CRITICAL":
                accountHealthLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                break;
        }
    }
    
    /**
     * Displays loan information (only for loan accounts)
     */
    private void displayLoanInfo() {
        LoanAccount loan = overviewService.getLoanAccountInfo(
            loggedInAccountNumber, loggedInAccountNumber
        );
        
        if (loan != null && loanInfoSection != null) {
            loanInfoSection.setVisible(true);
            loanInfoSection.setManaged(true);
            
            loanAmountLabel.setText(loan.getFormattedLoanAmount());
            outstandingBalanceLabel.setText(loan.getFormattedOutstandingBalance());
            interestRateLabel.setText(loan.getFormattedInterestRate());
            installmentAmountLabel.setText(loan.getFormattedInstallmentAmount());
            nextDueDateLabel.setText(loan.getNextDueDate());
            
            // Set repayment status with color coding
            repaymentStatusLabel.setText(loan.getRepaymentStatusDisplay());
            if (loan.isActive()) {
                repaymentStatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            } else if (loan.isOverdue()) {
                repaymentStatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            } else if (loan.isPaid()) {
                repaymentStatusLabel.setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
            }
            
            loanTermLabel.setText(loan.getLoanTerm() + " months");
            
            // Display repayment progress
            double progress = loan.getRepaymentPercentage();
            repaymentProgressLabel.setText(String.format("%.1f%% Repaid", progress));
            if (progress >= 75) {
                repaymentProgressLabel.setStyle("-fx-text-fill: #2e7d32;");
            } else if (progress >= 50) {
                repaymentProgressLabel.setStyle("-fx-text-fill: #1565c0;");
            } else if (progress >= 25) {
                repaymentProgressLabel.setStyle("-fx-text-fill: #f57c00;");
            } else {
                repaymentProgressLabel.setStyle("-fx-text-fill: #c62828;");
            }
        }
    }
    
    /**
     * Displays contact information
     */
    private void displayContactInfo(AccountOverview account) {
        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            emailLabel.setText(account.getEmail());
        } else {
            emailLabel.setText("Not provided");
            emailLabel.setStyle("-fx-text-fill: #9e9e9e;");
        }
        
        if (account.getPhoneNumber() != null && !account.getPhoneNumber().isEmpty()) {
            phoneLabel.setText(account.getPhoneNumber());
        } else {
            phoneLabel.setText("Not provided");
            phoneLabel.setStyle("-fx-text-fill: #9e9e9e;");
        }
    }
    
    /**
     * Refreshes the account overview
     */
    @FXML
    private void refreshOverview() {
        loadAccountOverview();
    }
    
    /**
     * Returns to dashboard
     */
    @FXML
    private void backToDashboard() {
        try {
            Main.changeScene("Dashboard.fxml");
        } catch (Exception e) {
            showError("Error returning to dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Shows success message
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
    }
    
    /**
     * Shows error message
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
    }
}
