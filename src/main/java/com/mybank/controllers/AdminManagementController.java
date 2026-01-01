package com.mybank.controllers;

import java.util.List;
import java.util.Optional;

import com.mybank.Main;
import com.mybank.models.Admin;
import com.mybank.services.AdminService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

/**
 * Controller for Admin Management (admins managing other admins).
 * Only accessible by super admins.
 */
public class AdminManagementController {
    
    private Admin currentAdmin;
    
    @FXML
    private TableView<Admin> adminTable;
    
    @FXML
    private TableColumn<Admin, Integer> idColumn;
    
    @FXML
    private TableColumn<Admin, String> usernameColumn;
    
    @FXML
    private TableColumn<Admin, String> fullNameColumn;
    
    @FXML
    private TableColumn<Admin, String> emailColumn;
    
    @FXML
    private TableColumn<Admin, String> statusColumn;
    
    @FXML
    private TableColumn<Admin, String> roleColumn;
    
    @FXML
    private void initialize() {
        currentAdmin = AdminService.getCurrentAdmin();
        
        if (currentAdmin == null || !currentAdmin.isSuperAdmin()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Super Admin Only");
            alert.setContentText("Only super administrators can access this module.");
            alert.showAndWait();
            Main.showAdminDashboard();
            return;
        }
        
        setupTable();
        loadAdmins();
    }
    
    private void setupTable() {
        if (adminTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("adminId"));
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            
            roleColumn.setCellValueFactory(cellData -> {
                String role = cellData.getValue().isSuperAdmin() ? "Super Admin" : "Admin";
                return new javafx.beans.property.SimpleStringProperty(role);
            });
            
            // Style status column
            statusColumn.setCellFactory(column -> new TableCell<Admin, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if ("ACTIVE".equals(item)) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        }
                    }
                }
            });
        }
    }
    
    private void loadAdmins() {
        if (adminTable != null) {
            List<Admin> admins = AdminService.getAllAdmins();
            if (admins != null) {
                ObservableList<Admin> adminList = FXCollections.observableArrayList(admins);
                adminTable.setItems(adminList);
            }
        }
    }
    
    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }
    
    @FXML
    private void handleCreateAdmin() {
        Dialog<Admin> dialog = new Dialog<>();
        dialog.setTitle("Create New Admin");
        dialog.setHeaderText("Enter admin details");
        
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        CheckBox superAdminCheck = new CheckBox("Super Admin");
        
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(superAdminCheck, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                boolean isSuperAdmin = superAdminCheck.isSelected();
                
                if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                    showError("All required fields must be filled!");
                    return null;
                }
                
                if (password.length() < 6) {
                    showError("Password must be at least 6 characters!");
                    return null;
                }
                
                boolean success = AdminService.createAdmin(username, password, fullName, email, phone, isSuperAdmin);
                
                if (success) {
                    showSuccess("Admin created successfully!");
                    loadAdmins();
                } else {
                    showError("Failed to create admin. Username or email may already exist.");
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    @FXML
    private void handleUpdateAdmin() {
        Admin selectedAdmin = adminTable != null ? adminTable.getSelectionModel().getSelectedItem() : null;
        
        if (selectedAdmin == null) {
            showError("Please select an admin to update.");
            return;
        }
        
        Dialog<Admin> dialog = new Dialog<>();
        dialog.setTitle("Update Admin");
        dialog.setHeaderText("Update admin details for: " + selectedAdmin.getUsername());
        
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField fullNameField = new TextField(selectedAdmin.getFullName());
        TextField emailField = new TextField(selectedAdmin.getEmail());
        TextField phoneField = new TextField(selectedAdmin.getPhone() != null ? selectedAdmin.getPhone() : "");
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        statusCombo.setValue(selectedAdmin.getStatus());
        
        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String status = statusCombo.getValue();
                
                if (fullName.isEmpty() || email.isEmpty()) {
                    showError("Full name and email are required!");
                    return null;
                }
                
                boolean success = AdminService.updateAdmin(selectedAdmin.getAdminId(), fullName, email, phone);
                if (success && !status.equals(selectedAdmin.getStatus())) {
                    success = AdminService.setAdminStatus(selectedAdmin.getAdminId(), status);
                }
                
                if (success) {
                    showSuccess("Admin updated successfully!");
                    loadAdmins();
                } else {
                    showError("Failed to update admin.");
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    @FXML
    private void handleDeleteAdmin() {
        Admin selectedAdmin = adminTable != null ? adminTable.getSelectionModel().getSelectedItem() : null;
        
        if (selectedAdmin == null) {
            showError("Please select an admin to delete.");
            return;
        }
        
        if (selectedAdmin.getAdminId() == currentAdmin.getAdminId()) {
            showError("You cannot delete your own account!");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Admin: " + selectedAdmin.getUsername());
        confirmAlert.setContentText("Are you sure you want to delete this admin?\n\n" +
            "Name: " + selectedAdmin.getFullName() + "\n" +
            "Email: " + selectedAdmin.getEmail() + "\n\n" +
            "This action cannot be undone!");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = AdminService.deleteAdmin(selectedAdmin.getAdminId());
            
            if (success) {
                showSuccess("Admin deleted successfully!");
                loadAdmins();
            } else {
                showError("Failed to delete admin.");
            }
        }
    }
    
    @FXML
    private void handleResetPassword() {
        Admin selectedAdmin = adminTable != null ? adminTable.getSelectionModel().getSelectedItem() : null;
        
        if (selectedAdmin == null) {
            showError("Please select an admin to reset password.");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + selectedAdmin.getUsername());
        dialog.setContentText("Enter new password:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            if (newPassword.length() < 6) {
                showError("Password must be at least 6 characters!");
                return;
            }
            
            boolean success = AdminService.resetPassword(selectedAdmin.getAdminId(), newPassword);
            
            if (success) {
                showSuccess("Password reset successfully!");
            } else {
                showError("Failed to reset password.");
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        loadAdmins();
        showInfo("Refreshed", "Admin list has been refreshed.");
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Operation Successful");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
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
}
