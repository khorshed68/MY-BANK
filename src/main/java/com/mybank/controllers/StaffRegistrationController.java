package com.mybank.controllers;

import java.io.File;

import com.mybank.Main;
import com.mybank.services.StaffService;
import com.mybank.utils.ProfilePictureManager;
import com.mybank.utils.ProfilePictureManager.ValidationResult;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Controller for staff registration.
 * Allows new staff members to create their own accounts.
 */
public class StaffRegistrationController {
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private ComboBox<String> roleComboBox;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private ImageView profilePicturePreview;
    
    @FXML
    private Label noImageLabel;
    
    @FXML
    private Button removePictureBtn;
    
    @FXML
    private Label messageLabel;
    
    private File selectedProfilePicture = null;
    
    /**
     * Handle registration
     */
    @FXML
    private void handleRegister() {
        // Get values
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = roleComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validation
        if (fullName.isEmpty()) {
            showError("Please enter your full name");
            fullNameField.requestFocus();
            return;
        }
        
        if (username.isEmpty()) {
            showError("Please enter a username");
            usernameField.requestFocus();
            return;
        }
        
        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            usernameField.requestFocus();
            return;
        }
        
        if (email.isEmpty()) {
            showError("Please enter your email");
            emailField.requestFocus();
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Please enter a valid email address");
            emailField.requestFocus();
            return;
        }
        
        if (phone.isEmpty()) {
            showError("Please enter your phone number");
            phoneField.requestFocus();
            return;
        }
        
        if (!phone.matches("^01[0-9]{9}$")) {
            showError("Please enter a valid phone number (01XXXXXXXXX)");
            phoneField.requestFocus();
            return;
        }
        
        if (role == null || role.isEmpty()) {
            showError("Please select a role");
            roleComboBox.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter a password");
            passwordField.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            passwordField.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            confirmPasswordField.clear();
            confirmPasswordField.requestFocus();
            return;
        }
        
        // Save profile picture if selected
        String profilePicturePath = null;
        if (selectedProfilePicture != null) {
            try {
                // Generate a temporary username-based ID for file naming
                String tempId = username + "_" + System.currentTimeMillis();
                profilePicturePath = ProfilePictureManager.saveProfilePictureForRequest(selectedProfilePicture, tempId);
            } catch (Exception e) {
                showError("Failed to save profile picture: " + e.getMessage());
                return;
            }
        }
        
        // Create staff account
        // Using createdBy = 0 for self-registration (no creator staff ID)
        boolean success = StaffService.createStaff(
            username, 
            password, 
            fullName, 
            email, 
            phone, 
            role, 
            0,  // Self-registration
            profilePicturePath
        );
        
        if (success) {
            showSuccess("Registration submitted successfully!\n\nYour account is pending admin approval.\nYou will be able to login once an administrator approves your registration.");
            
            // Clear form
            clearForm();
            
            // Redirect to login after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        Main.showStaffLogin();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } else {
            showError("Registration failed. Username may already exist. Please try a different username.");
        }
    }
    
    /**
     * Handle back to login
     */
    @FXML
    private void handleBack() {
        Main.showStaffLogin();
    }
    
    /**
     * Handle profile picture selection
     */
    @FXML
    private void chooseProfilePicture() {
        Stage stage = (Stage) profilePicturePreview.getScene().getWindow();
        File file = ProfilePictureManager.openFileChooser(stage);
        
        if (file != null) {
            ValidationResult validation = ProfilePictureManager.validateImage(file);
            
            if (validation.isValid()) {
                selectedProfilePicture = file;
                updateProfilePicturePreview(file);
                hideMessage();
            } else {
                showError(validation.getMessage());
            }
        }
    }
    
    /**
     * Handle remove profile picture
     */
    @FXML
    private void removeProfilePicture() {
        selectedProfilePicture = null;
        setDefaultPreview();
        hideMessage();
    }
    
    /**
     * Update profile picture preview
     * Automatically centers and crops any image to fit the circular shape
     */
    private void updateProfilePicturePreview(File imageFile) {
        try {
            Image image = new Image(imageFile.toURI().toString());
            profilePicturePreview.setImage(image);
            
            // Calculate viewport to center the image
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double size = Math.min(imageWidth, imageHeight);
            double offsetX = (imageWidth - size) / 2;
            double offsetY = (imageHeight - size) / 2;
            
            // Set viewport to show center square portion
            profilePicturePreview.setViewport(new javafx.geometry.Rectangle2D(offsetX, offsetY, size, size));
            
            profilePicturePreview.setPreserveRatio(false);
            profilePicturePreview.setFitWidth(94);
            profilePicturePreview.setFitHeight(94);
            profilePicturePreview.setSmooth(true);
            
            noImageLabel.setVisible(false);
            removePictureBtn.setVisible(true);
            removePictureBtn.setManaged(true);
        } catch (Exception e) {
            showError("Failed to load image preview");
        }
    }
    
    /**
     * Set default preview state
     */
    private void setDefaultPreview() {
        profilePicturePreview.setImage(null);
        profilePicturePreview.setViewport(null); // Reset viewport
        noImageLabel.setVisible(true);
        removePictureBtn.setVisible(false);
        removePictureBtn.setManaged(false);
    }
    
    /**
     * Clear form fields
     */
    private void clearForm() {
        fullNameField.clear();
        usernameField.clear();
        emailField.clear();
        phoneField.clear();
        roleComboBox.setValue(null);
        passwordField.clear();
        confirmPasswordField.clear();
        selectedProfilePicture = null;
        setDefaultPreview();
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
     * Hide message
     */
    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
    
    /**
     * Initialize controller
     */
    @FXML
    private void initialize() {
        hideMessage();
        setDefaultPreview();
        
        // Populate role ComboBox
        roleComboBox.getItems().addAll("TELLER", "OFFICER", "MANAGER");
        
        // Set default role
        roleComboBox.setValue("TELLER");
        
        // Add listeners to clear error on input
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> hideMessage());
    }
}
