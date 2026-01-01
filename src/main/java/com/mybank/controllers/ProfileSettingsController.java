package com.mybank.controllers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.utils.ProfilePictureManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Profile Settings Controller
 * Manages user profile picture and settings
 */
public class ProfileSettingsController {
    
    @FXML
    private Label accountNumberLabel;
    
    @FXML
    private Label nameLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label phoneLabel;
    
    @FXML
    private ImageView currentProfilePicture;
    
    @FXML
    private Label noCurrentImageLabel;
    
    @FXML
    private ImageView newProfilePicturePreview;
    
    @FXML
    private Label noNewImageLabel;
    
    @FXML
    private StackPane newProfilePictureContainer;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private PasswordField currentPasswordField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label passwordMessageLabel;
    
    private File selectedNewPicture = null;
    private String currentProfilePicturePath = null;
    private int accountNumber;
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        accountNumber = Main.getLoggedInAccount();
        loadAccountInformation();
        loadCurrentProfilePicture();
        clearNewPicturePreview();
    }
    
    /**
     * Load account information from database
     */
    private void loadAccountInformation() {
        String query = "SELECT ownerName, email, phoneNumber, profilePicturePath FROM accounts WHERE accountNumber = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                accountNumberLabel.setText(String.valueOf(accountNumber));
                nameLabel.setText(rs.getString("ownerName"));
                emailLabel.setText(rs.getString("email") != null ? rs.getString("email") : "Not set");
                phoneLabel.setText(rs.getString("phoneNumber") != null ? rs.getString("phoneNumber") : "Not set");
                currentProfilePicturePath = rs.getString("profilePicturePath");
            }
        } catch (Exception e) {
            System.err.println("Error loading account information: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load current profile picture
     */
    private void loadCurrentProfilePicture() {
        Image profileImage = ProfilePictureManager.loadProfilePicture(currentProfilePicturePath);
        currentProfilePicture.setImage(profileImage);
        
        // Set proper display settings for circular image
        currentProfilePicture.setPreserveRatio(false);
        currentProfilePicture.setFitWidth(146);
        currentProfilePicture.setFitHeight(146);
        currentProfilePicture.setSmooth(true);
        
        if (currentProfilePicturePath != null && !currentProfilePicturePath.isEmpty()) {
            noCurrentImageLabel.setVisible(false);
        } else {
            noCurrentImageLabel.setVisible(true);
        }
    }
    
    /**
     * Choose a new profile picture
     */
    @FXML
    private void chooseNewPicture() {
        try {
            Stage stage = (Stage) accountNumberLabel.getScene().getWindow();
            File file = ProfilePictureManager.openFileChooser(stage);
            
            if (file != null) {
                ProfilePictureManager.ValidationResult result = ProfilePictureManager.validateImage(file);
                
                if (result.isValid()) {
                    selectedNewPicture = file;
                    updateNewPicturePreview(file);
                    saveButton.setDisable(false);
                    showMessage("✓ New picture selected. Click 'Save Changes' to apply.", "#2e7d32");
                } else {
                    showError(result.getMessage());
                    selectedNewPicture = null;
                    clearNewPicturePreview();
                }
            }
        } catch (Exception e) {
            showError("Error selecting picture: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Save new profile picture to database
     */
    @FXML
    private void saveProfilePicture() {
        if (selectedNewPicture == null) {
            showError("No new picture selected!");
            return;
        }
        
        try {
            // Save the new profile picture
            String newPicturePath = ProfilePictureManager.saveProfilePicture(selectedNewPicture, accountNumber);
            
            // Update database
            String updateQuery = "UPDATE accounts SET profilePicturePath = ? WHERE accountNumber = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                
                pstmt.setString(1, newPicturePath);
                pstmt.setInt(2, accountNumber);
                int rowsUpdated = pstmt.executeUpdate();
                
                if (rowsUpdated > 0) {
                    // Delete old profile picture if it exists
                    if (currentProfilePicturePath != null && !currentProfilePicturePath.isEmpty()) {
                        ProfilePictureManager.deleteProfilePicture(currentProfilePicturePath);
                    }
                    
                    // Update current picture
                    currentProfilePicturePath = newPicturePath;
                    loadCurrentProfilePicture();
                    
                    // Clear new picture preview
                    selectedNewPicture = null;
                    clearNewPicturePreview();
                    saveButton.setDisable(true);
                    
                    showSuccess("✓ Profile picture updated successfully!");
                } else {
                    showError("Failed to update profile picture in database.");
                }
            }
        } catch (Exception e) {
            showError("Error saving profile picture: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Remove current profile picture
     */
    @FXML
    private void removePicture() {
        try {
            // Update database to set profile picture to null
            String updateQuery = "UPDATE accounts SET profilePicturePath = NULL WHERE accountNumber = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                
                pstmt.setInt(1, accountNumber);
                int rowsUpdated = pstmt.executeUpdate();
                
                if (rowsUpdated > 0) {
                    // Delete the file
                    if (currentProfilePicturePath != null && !currentProfilePicturePath.isEmpty()) {
                        ProfilePictureManager.deleteProfilePicture(currentProfilePicturePath);
                    }
                    
                    // Update current picture
                    currentProfilePicturePath = null;
                    loadCurrentProfilePicture();
                    
                    showSuccess("✓ Profile picture removed successfully!");
                } else {
                    showError("Failed to remove profile picture.");
                }
            }
        } catch (Exception e) {
            showError("Error removing profile picture: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update new picture preview
     * Automatically centers and crops any image to fit the circular shape
     */
    private void updateNewPicturePreview(File imageFile) {
        if (imageFile != null) {
            try {
                // Load the original image
                Image image = new Image(imageFile.toURI().toString());
                newProfilePicturePreview.setImage(image);
                
                // Calculate viewport to center the image
                double imageWidth = image.getWidth();
                double imageHeight = image.getHeight();
                double size = Math.min(imageWidth, imageHeight);
                double offsetX = (imageWidth - size) / 2;
                double offsetY = (imageHeight - size) / 2;
                
                // Set viewport to show center square portion
                newProfilePicturePreview.setViewport(new javafx.geometry.Rectangle2D(offsetX, offsetY, size, size));
                
                // Set proper display settings for circular image
                newProfilePicturePreview.setPreserveRatio(false);
                newProfilePicturePreview.setFitWidth(154);
                newProfilePicturePreview.setFitHeight(154);
                newProfilePicturePreview.setSmooth(true);
                
                noNewImageLabel.setVisible(false);
            } catch (Exception e) {
                System.err.println("Error loading image preview: " + e.getMessage());
                clearNewPicturePreview();
            }
        } else {
            clearNewPicturePreview();
        }
    }
    
    /**
     * Clear new picture preview
     */
    private void clearNewPicturePreview() {
        newProfilePicturePreview.setImage(null);
        newProfilePicturePreview.setViewport(null); // Reset viewport
        newProfilePicturePreview.setPreserveRatio(true); // Reset to default
        noNewImageLabel.setVisible(true);
        saveButton.setDisable(true);
    }
    
    /**
     * Go back to dashboard
     */
    @FXML
    private void backToDashboard() {
        try {
            Main.changeScene("Dashboard.fxml");
        } catch (Exception e) {
            System.err.println("Error returning to dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle password change
     */
    @FXML
    private void handleChangePassword() {
        // Clear previous messages
        passwordMessageLabel.setText("");
        
        // Get input values
        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        
        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showPasswordError("❌ All password fields are required!");
            return;
        }
        
        // Validate new password length
        if (newPassword.length() < 6) {
            showPasswordError("❌ New password must be at least 6 characters!");
            return;
        }
        
        // Validate password confirmation
        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("❌ New passwords do not match!");
            return;
        }
        
        // Validate current password
        DatabaseHelper dbHelper = new DatabaseHelper();
        if (!dbHelper.authenticateLogin(accountNumber, currentPassword)) {
            showPasswordError("❌ Current password is incorrect!");
            return;
        }
        
        // Check if new password is same as current
        if (currentPassword.equals(newPassword)) {
            showPasswordError("❌ New password must be different from current password!");
            return;
        }
        
        // Update password in database
        if (dbHelper.updatePassword(accountNumber, newPassword)) {
            showPasswordSuccess("✓ Password changed successfully!");
            
            // Clear all password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            
            // Send notification (optional)
            try {
                com.mybank.services.NotificationService notificationService = new com.mybank.services.NotificationService();
                notificationService.sendPasswordChangeNotification(accountNumber);
            } catch (Exception e) {
                System.out.println("Could not send password change notification: " + e.getMessage());
                // Don't show error to user as password change was successful
            }
        } else {
            showPasswordError("❌ Failed to update password. Please try again.");
        }
    }
    
    /**
     * Show password error message
     */
    private void showPasswordError(String message) {
        passwordMessageLabel.setText(message);
        passwordMessageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
    }
    
    /**
     * Show password success message
     */
    private void showPasswordSuccess(String message) {
        passwordMessageLabel.setText(message);
        passwordMessageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
    }
    
    /**
     * Show general message
     */
    private void showMessage(String message, String color) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }
}
