package com.mybank.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mybank.Main;
import com.mybank.models.AccountRequest;
import com.mybank.models.Staff;
import com.mybank.services.AccountRequestService;

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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * Controller for account approval queue.
 * Allows staff to view, approve, or reject pending account requests.
 */
public class AccountApprovalController {
    
    @FXML
    private TableView<AccountRequest> requestsTable;
    
    @FXML
    private TableColumn<AccountRequest, Integer> requestIdColumn;
    
    @FXML
    private TableColumn<AccountRequest, String> customerNameColumn;
    
    @FXML
    private TableColumn<AccountRequest, String> phoneColumn;
    
    @FXML
    private TableColumn<AccountRequest, String> accountTypeColumn;
    
    @FXML
    private TableColumn<AccountRequest, String> depositColumn;
    
    @FXML
    private TableColumn<AccountRequest, String> requestDateColumn;
    
    @FXML
    private TableColumn<AccountRequest, String> statusColumn;
    
    @FXML
    private TableColumn<AccountRequest, Void> actionsColumn;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private Label pendingCountLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private VBox detailsPanel;
    
    @FXML
    private Label detailCustomerName;
    
    @FXML
    private Label detailEmail;
    
    @FXML
    private Label detailPhone;
    
    @FXML
    private Label detailAddress;
    
    @FXML
    private Label detailIdentityType;
    
    @FXML
    private Label detailIdentityNumber;
    
    @FXML
    private Label detailAccountType;
    
    @FXML
    private Label detailDeposit;
    
    private Staff currentStaff;
    private AccountRequest selectedRequest;
    private ObservableList<AccountRequest> requestsList;
    
    /**
     * Initialize the controller
     * Automatically loads pending requests when page opens
     */
    @FXML
    private void initialize() {
        currentStaff = Main.getCurrentStaff();
        
        // Populate status filter ComboBox
        statusFilter.getItems().addAll("All", "PENDING", "APPROVED", "REJECTED");
        
        // Set default filter to PENDING first (before loading)
        statusFilter.setValue("PENDING");
        
        // Setup table structure
        setupTable();
        
        // Automatically load pending requests on page load
        loadRequests();
        
        // Setup filter change listener for future manual filtering
        statusFilter.setOnAction(e -> loadRequests());
        
        System.out.println("Account Approval Queue initialized - Auto-loaded pending requests");
    }
    
    /**
     * Setup table columns
     */
    private void setupTable() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        accountTypeColumn.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        
        // Format deposit column
        depositColumn.setCellValueFactory(new PropertyValueFactory<>("formattedInitialDeposit"));
        
        // Format date column
        requestDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedRequestDate"));
        
        // Status column with color coding
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("requestStatus"));
        statusColumn.setCellFactory(column -> new TableCell<AccountRequest, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "PENDING":
                            setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                            break;
                        case "APPROVED":
                            setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                            break;
                        case "REJECTED":
                            setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
        // Actions column with view button
        actionsColumn.setCellFactory(column -> new TableCell<AccountRequest, Void>() {
            private final Button viewBtn = new Button("View Details");
            
            {
                viewBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; " +
                               "-fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                viewBtn.setOnAction(event -> {
                    AccountRequest request = getTableView().getItems().get(getIndex());
                    showRequestDetails(request);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });
    }
    
    /**
     * Load account requests based on current filter
     * Called automatically on page load and manually via refresh button
     */
    private void loadRequests() {
        try {
            String filter = statusFilter.getValue();
            List<AccountRequest> requests;
            
            // Load requests based on filter selection
            if ("PENDING".equals(filter)) {
                requests = AccountRequestService.getPendingRequests();
            } else if ("All".equals(filter)) {
                requests = AccountRequestService.getAllRequests(100);
            } else {
                requests = AccountRequestService.getAllRequests(100).stream()
                    .filter(r -> r.getRequestStatus().equals(filter))
                    .collect(Collectors.toList());
            }
            
            // Update table with fresh data
            requestsList = FXCollections.observableArrayList(requests);
            requestsTable.setItems(requestsList);
            
            // Update pending count in header (always show total pending regardless of filter)
            long pendingCount = requests.stream()
                .filter(AccountRequest::isPending)
                .count();
            pendingCountLabel.setText("Pending: " + pendingCount);
            
            // Update status label
            statusLabel.setText("Loaded " + requests.size() + " " + filter + " request(s)");
            
            System.out.println("Loaded " + requests.size() + " requests with filter: " + filter);
            
        } catch (Exception e) {
            System.err.println("Error loading requests: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error loading requests");
        }
    }
    
    /**
     * Show request details
     */
    private void showRequestDetails(AccountRequest request) {
        selectedRequest = request;
        
        detailCustomerName.setText(request.getCustomerName());
        detailEmail.setText(request.getEmail() != null ? request.getEmail() : "N/A");
        detailPhone.setText(request.getPhoneNumber());
        detailAddress.setText(request.getAddress() != null ? request.getAddress() : "N/A");
        detailIdentityType.setText(request.getIdentityTypeDisplay());
        detailIdentityNumber.setText(request.getMaskedIdentityNumber());
        detailAccountType.setText(request.getAccountTypeDisplay());
        detailDeposit.setText(request.getFormattedInitialDeposit());
        
        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);
    }
    
    /**
     * Handle approve request
     */
    @FXML
    private void handleApprove() {
        if (selectedRequest == null || !selectedRequest.isPending()) {
            showAlert("Error", "Please select a pending request", Alert.AlertType.ERROR);
            return;
        }
        
        // Confirm approval
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Approval");
        confirmAlert.setHeaderText("Approve Account Request");
        confirmAlert.setContentText("Are you sure you want to approve this account request for " + 
                                   selectedRequest.getCustomerName() + "?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Approve request with default password
            boolean success = AccountRequestService.approveRequest(
                selectedRequest.getRequestId(),
                currentStaff.getStaffId()
            );
            
            if (success) {
                showAlert("Success", 
                         "Account request approved successfully!\n" +
                         "Account has been created.\n" +
                         "Customer should change the default password on first login.", 
                         Alert.AlertType.INFORMATION);
                handleCloseDetails();
                loadRequests();
            } else {
                showAlert("Error", "Failed to approve request. Please try again.", 
                         Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Handle reject request
     */
    @FXML
    private void handleReject() {
        if (selectedRequest == null || !selectedRequest.isPending()) {
            showAlert("Error", "Please select a pending request", Alert.AlertType.ERROR);
            return;
        }
        
        // Request rejection reason
        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Reject Request");
        reasonDialog.setHeaderText("Rejection Reason");
        reasonDialog.setContentText("Enter reason for rejection:");
        
        Optional<String> result = reasonDialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String reason = result.get().trim();
            
            // Reject request
            boolean success = AccountRequestService.rejectRequest(
                selectedRequest.getRequestId(),
                currentStaff.getStaffId(),
                reason
            );
            
            if (success) {
                showAlert("Success", "Account request rejected.", Alert.AlertType.INFORMATION);
                handleCloseDetails();
                loadRequests();
            } else {
                showAlert("Error", "Failed to reject request. Please try again.", 
                         Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Handle close details panel
     */
    @FXML
    private void handleCloseDetails() {
        detailsPanel.setVisible(false);
        detailsPanel.setManaged(false);
        selectedRequest = null;
    }
    
    /**
     * Handle manual refresh button click
     * Reloads requests based on current filter selection
     */
    @FXML
    private void handleRefresh() {
        loadRequests();
        
        // Show user feedback
        String filter = statusFilter.getValue();
        statusLabel.setText("Refreshed - Showing " + filter + " requests");
        System.out.println("Manual refresh triggered - Reloading requests");
    }
    
    /**
     * Handle back to dashboard
     */
    @FXML
    private void handleBackToDashboard() {
        Main.showStaffDashboard();
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
