package com.mybank.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.models.Admin;
import com.mybank.models.Staff;
import com.mybank.services.AdminService;
import com.mybank.services.StaffService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

/**
 * Controller for Admin Staff Management.
 * Allows admins to manage bank staff accounts.
 */
public class AdminStaffManagementController {
    
    @FXML
    private TableView<StaffRecord> staffTable;
    
    @FXML
    private TableColumn<StaffRecord, String> staffIdColumn;
    
    @FXML
    private TableColumn<StaffRecord, String> usernameColumn;
    
    @FXML
    private TableColumn<StaffRecord, String> fullNameColumn;
    
    @FXML
    private TableColumn<StaffRecord, String> emailColumn;
    
    @FXML
    private TableColumn<StaffRecord, String> roleColumn;
    
    @FXML
    private TableColumn<StaffRecord, String> statusColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> filterComboBox;
    
    @FXML
    private Label messageLabel;
    
    private ObservableList<StaffRecord> staffList = FXCollections.observableArrayList();
    private Admin currentAdmin;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        currentAdmin = AdminService.getCurrentAdmin();
        
        if (currentAdmin == null) {
            Main.showAdminLogin();
            return;
        }
        
        // Setup table columns
        staffIdColumn.setCellValueFactory(data -> data.getValue().staffIdProperty());
        usernameColumn.setCellValueFactory(data -> data.getValue().usernameProperty());
        fullNameColumn.setCellValueFactory(data -> data.getValue().fullNameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        roleColumn.setCellValueFactory(data -> data.getValue().roleProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        
        // Setup filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList("All", "PENDING", "ACTIVE", "INACTIVE", "TELLER", "OFFICER", "MANAGER"));
        filterComboBox.setValue("All");
        
        // Load staff data
        loadStaffData();
        
        // Add search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterStaffData());
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterStaffData());
    }
    
    /**
     * Load all staff data
     */
    private void loadStaffData() {
        staffList.clear();
        
        try {
            Connection conn = DatabaseHelper.getConnection();
            String sql = "SELECT staffId, username, fullName, email, phoneNumber, role, status FROM staff ORDER BY staffId DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                staffList.add(new StaffRecord(
                    rs.getInt("staffId"),
                    rs.getString("username"),
                    rs.getString("fullName"),
                    rs.getString("email"),
                    rs.getString("phoneNumber"),
                    rs.getString("role"),
                    rs.getString("status")
                ));
            }
            
            staffTable.setItems(staffList);
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load staff data: " + e.getMessage());
        }
    }
    
    /**
     * Filter staff data based on search and filter criteria
     */
    private void filterStaffData() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filter = filterComboBox.getValue();
        
        ObservableList<StaffRecord> filtered = FXCollections.observableArrayList();
        
        for (StaffRecord staff : staffList) {
            boolean matchesSearch = searchText.isEmpty() || 
                staff.getUsername().toLowerCase().contains(searchText) ||
                staff.getFullName().toLowerCase().contains(searchText) ||
                staff.getEmail().toLowerCase().contains(searchText);
            
            boolean matchesFilter = filter.equals("All") ||
                staff.getStatus().equals(filter) ||
                staff.getRole().equals(filter);
            
            if (matchesSearch && matchesFilter) {
                filtered.add(staff);
            }
        }
        
        staffTable.setItems(filtered);
    }
    
    /**
     * Handle approve pending staff registration
     */
    @FXML
    private void handleApproveStaff() {
        StaffRecord selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a staff member");
            return;
        }
        
        if (!selected.getStatus().equals(Staff.STATUS_PENDING)) {
            showError("Only pending staff can be approved");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Approval");
        confirm.setHeaderText("Approve Staff Registration");
        confirm.setContentText("Are you sure you want to approve " + selected.getFullName() + " as " + selected.getRole() + "?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateStaffStatus(selected.getStaffId(), "ACTIVE");
            showSuccess(selected.getFullName() + " has been approved and can now login");
        }
    }
    
    /**
     * Handle reject pending staff registration
     */
    @FXML
    private void handleRejectStaff() {
        StaffRecord selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a staff member");
            return;
        }
        
        if (!selected.getStatus().equals(Staff.STATUS_PENDING)) {
            showError("Only pending staff can be rejected");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Rejection");
        confirm.setHeaderText("Reject Staff Registration");
        confirm.setContentText("Are you sure you want to reject " + selected.getFullName() + "'s registration?\nThis will delete their account.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connection conn = DatabaseHelper.getConnection();
                String sql = "DELETE FROM staff WHERE staffId = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, selected.getStaffId());
                pstmt.executeUpdate();
                
                AdminService.logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(),
                    "REJECT_REGISTRATION", "STAFF", "Rejected staff registration: " + selected.getUsername(), "SUCCESS");
                
                showSuccess("Staff registration rejected and deleted");
                loadStaffData();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to reject staff: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle create new staff
     */
    @FXML
    private void handleCreateStaff() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Staff");
        dialog.setHeaderText("Enter staff details");
        
        // Create form
        VBox content = new VBox(10);
        content.setPrefWidth(400);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.setItems(FXCollections.observableArrayList("TELLER", "OFFICER", "MANAGER"));
        roleCombo.setPromptText("Select Role");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        content.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Full Name:"), fullNameField,
            new Label("Email:"), emailField,
            new Label("Phone:"), phoneField,
            new Label("Role:"), roleCombo,
            new Label("Password:"), passwordField
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String role = roleCombo.getValue();
            String password = passwordField.getText();
            
            if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || role == null || password.isEmpty()) {
                showError("All fields are required");
                return;
            }
            
            // Use StaffService to create staff (admin creates, so createdBy = currentAdmin.getAdminId(), no profile picture)
            boolean success = StaffService.createStaff(
                username,
                password,
                fullName,
                email,
                phone,
                role,
                currentAdmin.getAdminId(), // Created by admin
                null // No profile picture from admin interface
            );
            
            if (success) {
                AdminService.logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(),
                    "CREATE", "STAFF", "Created staff: " + username, "SUCCESS");
                
                showSuccess("Staff created successfully");
                loadStaffData();
            } else {
                showError("Failed to create staff. Username may already exist.");
            }
        }
    }
    
    /**
     * Handle activate staff
     */
    @FXML
    private void handleActivateStaff() {
        StaffRecord selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a staff member");
            return;
        }
        
        updateStaffStatus(selected.getStaffId(), Staff.STATUS_ACTIVE);
    }
    
    /**
     * Handle deactivate staff
     */
    @FXML
    private void handleDeactivateStaff() {
        StaffRecord selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a staff member");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deactivation");
        confirm.setHeaderText("Deactivate Staff Member");
        confirm.setContentText("Are you sure you want to deactivate " + selected.getFullName() + "?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateStaffStatus(selected.getStaffId(), Staff.STATUS_INACTIVE);
        }
    }
    
    /**
     * Handle reset password
     */
    @FXML
    private void handleResetPassword() {
        StaffRecord selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a staff member");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for " + selected.getFullName());
        dialog.setContentText("New Password:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newPassword = result.get().trim();
            
            try {
                Connection conn = DatabaseHelper.getConnection();
                String sql = "UPDATE staff SET passwordHash = ? WHERE staffId = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, hashPassword(newPassword));
                pstmt.setInt(2, selected.getStaffId());
                pstmt.executeUpdate();
                
                AdminService.logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(),
                    "PASSWORD_RESET", "STAFF", "Reset password for staff: " + selected.getUsername(), "SUCCESS");
                
                showSuccess("Password reset successfully");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to reset password: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle view activity log
     */
    @FXML
    private void handleViewActivity() {
        StaffRecord selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a staff member");
            return;
        }
        
        // Navigate to audit logs filtered by this staff member
        Main.showAdminAuditLogs();
    }
    
    /**
     * Update staff status
     */
    private void updateStaffStatus(int staffId, String status) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String sql = "UPDATE staff SET status = ? WHERE staffId = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, staffId);
            pstmt.executeUpdate();
            
            AdminService.logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(),
                "STATUS_CHANGE", "STAFF", "Changed staff status to " + status + " for staff ID: " + staffId, "SUCCESS");
            
            showSuccess("Staff status updated to " + status);
            loadStaffData();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to update staff status: " + e.getMessage());
        }
    }
    
    /**
     * Hash password
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return password;
        }
    }
    
    /**
     * Handle back to dashboard
     */
    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    /**
     * StaffRecord class for TableView
     */
    public static class StaffRecord {
        private final int staffId;
        private final SimpleStringProperty username;
        private final SimpleStringProperty fullName;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty role;
        private final SimpleStringProperty status;
        
        public StaffRecord(int staffId, String username, String fullName, String email, 
                         String phone, String role, String status) {
            this.staffId = staffId;
            this.username = new SimpleStringProperty(username);
            this.fullName = new SimpleStringProperty(fullName);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
            this.role = new SimpleStringProperty(role);
            this.status = new SimpleStringProperty(status);
        }
        
        public int getStaffId() { return staffId; }
        public String getUsername() { return username.get(); }
        public String getFullName() { return fullName.get(); }
        public String getEmail() { return email.get(); }
        public String getPhone() { return phone.get(); }
        public String getRole() { return role.get(); }
        public String getStatus() { return status.get(); }
        
        public SimpleStringProperty staffIdProperty() { return new SimpleStringProperty(String.valueOf(staffId)); }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty fullNameProperty() { return fullName; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty roleProperty() { return role; }
        public SimpleStringProperty statusProperty() { return status; }
    }
}
