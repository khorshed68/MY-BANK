package com.mybank.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.mybank.Main;
import com.mybank.database.AdminDAO;
import com.mybank.models.Admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

/**
 * Controller for Admin Registration screen.
 */
public class AdminRegistrationController {
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private ImageView profileImageView;
    
    @FXML
    private Label profileImageLabel;
    
    private File selectedImageFile;
    private static final String PROFILE_PICTURES_DIR = "profile_pictures/admins/";
    
    /**
     * Handle registration
     */
    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
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
        
        if (username.length() < 4) {
            showError("Username must be at least 4 characters");
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
            showError("Phone number must be 11 digits starting with 01");
            phoneField.requestFocus();
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
            confirmPasswordField.requestFocus();
            return;
        }
        
        // Check if username already exists
        if (AdminDAO.findByUsername(username) != null) {
            showError("Username already exists. Please choose a different username.");
            usernameField.requestFocus();
            return;
        }
        
        // Check if email already exists
        if (AdminDAO.findByEmail(email) != null) {
            showError("Email already registered. Please use a different email.");
            emailField.requestFocus();
            return;
        }
        
        // Create admin account
        try {
            Admin admin = new Admin();
            admin.setFullName(fullName);
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPhone(phone);
            admin.setPassword(hashPassword(password));
            admin.setStatus("ACTIVE");
            admin.setCreatedBy(username); // Self-registered
            admin.setSuperAdmin(false); // Regular admin, not super admin
            
            // Handle profile picture upload
            String profilePicturePath = null;
            if (selectedImageFile != null) {
                profilePicturePath = saveProfilePicture(username);
                if (profilePicturePath != null) {
                    admin.setProfilePicturePath(profilePicturePath);
                }
            }
            
            boolean success = AdminDAO.insertAdmin(admin);
            
            if (success) {
                showSuccess("Registration successful! You can now login.");
                
                // Clear fields
                clearFields();
                
                // Navigate to login after a short delay
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            Main.showAdminLogin();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                
            } else {
                showError("Registration failed. Please try again.");
            }
            
        } catch (Exception e) {
            showError("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle back to login
     */
    @FXML
    private void handleBack() {
        Main.showAdminLogin();
    }
    
    /**
     * Handle profile picture upload
     */
    @FXML
    private void handleUploadPicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (file != null) {
            try {
                // Load image
                Image image = new Image(file.toURI().toString());
                profileImageView.setImage(image);
                
                // Calculate viewport to center the image
                double imageWidth = image.getWidth();
                double imageHeight = image.getHeight();
                double size = Math.min(imageWidth, imageHeight);
                double offsetX = (imageWidth - size) / 2;
                double offsetY = (imageHeight - size) / 2;
                
                // Set viewport to show center square portion
                profileImageView.setViewport(new javafx.geometry.Rectangle2D(offsetX, offsetY, size, size));
                
                profileImageView.setPreserveRatio(false);
                profileImageView.setFitWidth(94);
                profileImageView.setFitHeight(94);
                profileImageView.setSmooth(true);
                
                selectedImageFile = file;
                profileImageLabel.setText("Picture selected ✓");
                profileImageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 10px; -fx-font-weight: 500;");
            } catch (Exception e) {
                showError("Failed to load image: " + e.getMessage());
            }
        }
    }
    
    /**
     * Save profile picture to filesystem
     */
    private String saveProfilePicture(String username) {
        try {
            // Create directory if it doesn't exist
            Path directory = Paths.get(PROFILE_PICTURES_DIR);
            Files.createDirectories(directory);
            
            // Generate unique filename
            String fileExtension = getFileExtension(selectedImageFile.getName());
            String filename = username + "_" + System.currentTimeMillis() + "." + fileExtension;
            Path targetPath = directory.resolve(filename);
            
            // Copy file
            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return PROFILE_PICTURES_DIR + filename;
        } catch (IOException e) {
            showError("Failed to save profile picture: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "jpg";
    }
    
    /**
     * Clear all input fields
     */
    private void clearFields() {
        fullNameField.clear();
        usernameField.clear();
        emailField.clear();
        phoneField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        selectedImageFile = null;
        profileImageView.setImage(null);
        profileImageView.setViewport(null); // Reset viewport
        profileImageLabel.setText("No Image");
        profileImageLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 10px;");
        profileImageLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 9px;");
    }
    
    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
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
        
        // Setup profile image view with circular clip
        if (profileImageView != null) {
            Circle clip = new Circle(40, 40, 40);
            profileImageView.setClip(clip);
        }
        
        // Add listeners to clear error on input
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> hideMessage());
    }
}
