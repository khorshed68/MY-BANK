package com.mybank.controllers;

import java.io.File;

import com.mybank.Main;
import com.mybank.models.AccountRequest;
import com.mybank.services.AccountRequestService;
import com.mybank.utils.ProfilePictureManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Create Account Controller
 * Handles customer account request submission
 * Accounts must be approved by staff before activation
 */
public class CreateAccountController {
    
    @FXML
    private TextField customerNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneNumberField;
    
    @FXML
    private TextField addressField;
    
    @FXML
    private ComboBox<String> identityTypeComboBox;
    
    @FXML
    private TextField identityNumberField;
    
    @FXML
    private ComboBox<String> accountTypeComboBox;
    
    @FXML
    private TextField initialDepositField;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private ImageView profilePicturePreview;
    
    @FXML
    private Label noImageLabel;
    
    @FXML
    private Button removeImageButton;
    
    @FXML
    private StackPane profilePictureContainer;
    
    // Store selected profile picture file
    private File selectedProfilePicture = null;
    
    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        // Populate identity type options
        identityTypeComboBox.getItems().addAll(
            "National ID",
            "Passport",
            "Driving License"
        );
        
        // Populate account type options
        accountTypeComboBox.getItems().addAll(
            "SAVINGS",
            "CURRENT"
        );
        
        // Set default selections
        identityTypeComboBox.setValue("National ID");
        accountTypeComboBox.setValue("SAVINGS");
        
        // Initialize profile picture preview with default state
        updateProfilePicturePreview(null);
    }
    
    /**
     * Handle profile picture selection
     */
    @FXML
    private void chooseProfilePicture() {
        try {
            Stage stage = (Stage) customerNameField.getScene().getWindow();
            File file = ProfilePictureManager.openFileChooser(stage);
            
            if (file != null) {
                // Validate the selected image
                ProfilePictureManager.ValidationResult result = ProfilePictureManager.validateImage(file);
                
                if (result.isValid()) {
                    selectedProfilePicture = file;
                    updateProfilePicturePreview(file);
                    messageLabel.setText("✓ Profile picture selected: " + file.getName());
                    messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                } else {
                    showError(result.getMessage());
                    selectedProfilePicture = null;
                }
            }
        } catch (Exception e) {
            showError("Error selecting profile picture: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Remove selected profile picture
     */
    @FXML
    private void removeProfilePicture() {
        selectedProfilePicture = null;
        updateProfilePicturePreview(null);
        messageLabel.setText("Profile picture removed");
        messageLabel.setStyle("-fx-text-fill: #666;");
    }
    
    /**
     * Update profile picture preview
     * Automatically centers and crops any image (square or rectangle) to fit the circular shape
     */
    private void updateProfilePicturePreview(File imageFile) {
        if (imageFile != null) {
            try {
                // Load the original image
                Image image = new Image(imageFile.toURI().toString());
                
                // Set the image to ImageView with proper settings for circular display
                profilePicturePreview.setImage(image);
                
                // Calculate viewport to center the image
                double imageWidth = image.getWidth();
                double imageHeight = image.getHeight();
                double size = Math.min(imageWidth, imageHeight);
                double offsetX = (imageWidth - size) / 2;
                double offsetY = (imageHeight - size) / 2;
                
                // Set viewport to show center square portion of the image
                profilePicturePreview.setViewport(new javafx.geometry.Rectangle2D(offsetX, offsetY, size, size));
                
                // Disable preserve ratio and set to fill the circular container
                profilePicturePreview.setPreserveRatio(false);
                profilePicturePreview.setFitWidth(94);
                profilePicturePreview.setFitHeight(94);
                profilePicturePreview.setSmooth(true);
                
                noImageLabel.setVisible(false);
                removeImageButton.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error loading image preview: " + e.getMessage());
                setDefaultPreview();
            }
        } else {
            setDefaultPreview();
        }
    }
    
    /**
     * Set default preview state
     */
    private void setDefaultPreview() {
        profilePicturePreview.setImage(null);
        profilePicturePreview.setViewport(null); // Reset viewport
        profilePicturePreview.setPreserveRatio(true); // Reset to default
        noImageLabel.setVisible(true);
        removeImageButton.setVisible(false);
    }
    
    /**
     * Submit account request for staff approval
     */
    @FXML
    private void createAccount() {
        // Clear previous message
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: black;");
        
        try {
            // Validate inputs
            if (customerNameField.getText().trim().isEmpty()) {
                showError("Please enter your full name!");
                return;
            }
            
            if (phoneNumberField.getText().trim().isEmpty()) {
                showError("Please enter your phone number!");
                return;
            }
            
            if (identityTypeComboBox.getValue() == null) {
                showError("Please select identity type!");
                return;
            }
            
            if (identityNumberField.getText().trim().isEmpty()) {
                showError("Please enter your identity number!");
                return;
            }
            
            if (accountTypeComboBox.getValue() == null) {
                showError("Please select account type!");
                return;
            }
            
            if (initialDepositField.getText().trim().isEmpty()) {
                showError("Please enter initial deposit amount!");
                return;
            }
            
            // Parse values
            String customerName = customerNameField.getText().trim();
            String email = emailField.getText().trim();
            String phoneNumber = phoneNumberField.getText().trim();
            String address = addressField.getText().trim();
            String identityType = convertIdentityType(identityTypeComboBox.getValue());
            String identityNumber = identityNumberField.getText().trim();
            String accountType = accountTypeComboBox.getValue();
            double initialDeposit = Double.parseDouble(initialDepositField.getText().trim());
            
            // Validate phone number format
            if (!phoneNumber.matches("^01[0-9]{9}$")) {
                showError("Please enter a valid phone number (01XXXXXXXXX)!");
                return;
            }
            
            // Validate email if provided
            if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showError("Please enter a valid email address!");
                return;
            }
            
            // Validate initial deposit
            if (initialDeposit < 0) {
                showError("Initial deposit cannot be negative!");
                return;
            }
            
            // Save profile picture if selected
            String profilePicturePath = null;
            if (selectedProfilePicture != null) {
                try {
                    // Generate temporary ID for file naming (using timestamp)
                    String tempId = String.valueOf(System.currentTimeMillis());
                    profilePicturePath = ProfilePictureManager.saveProfilePictureForRequest(
                        selectedProfilePicture, tempId
                    );
                    System.out.println("Profile picture saved: " + profilePicturePath);
                } catch (Exception e) {
                    System.err.println("Error saving profile picture: " + e.getMessage());
                    e.printStackTrace();
                    // Continue with account creation even if profile picture fails
                }
            }
            
            // Create account request
            AccountRequest request = new AccountRequest(
                customerName, email, phoneNumber, address,
                identityType, identityNumber, accountType, initialDeposit
            );
            request.setProfilePicturePath(profilePicturePath);
            
            boolean success = AccountRequestService.createAccountRequest(request);
            
            if (success) {
                showSuccess("Account request submitted successfully!\n\n" +
                           "Your application has been sent to staff for approval.\n" +
                           "You will be notified once your account is activated.\n\n" +
                           "Name: " + customerName + "\n" +
                           "Account Type: " + accountType + "\n" +
                           "Initial Deposit: ৳" + String.format("%.2f", initialDeposit));
                clearFields();
            } else {
                showError("Failed to submit account request. Please try again.");
            }
            
        } catch (NumberFormatException e) {
            showError("Invalid input! Please enter valid numbers.");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Convert display identity type to database value
     */
    private String convertIdentityType(String displayType) {
        switch (displayType) {
            case "National ID":
                return AccountRequest.ID_NATIONAL;
            case "Passport":
                return AccountRequest.ID_PASSPORT;
            case "Driving License":
                return AccountRequest.ID_DRIVING_LICENSE;
            default:
                return displayType;
        }
    }
    
    /**
     * Returns to dashboard or login
     */
    @FXML
    private void backToDashboard() {
        try {
            // If logged in, go to dashboard; otherwise go to login
            if (Main.isLoggedIn()) {
                Main.changeScene("Dashboard.fxml");
            } else {
                Main.changeScene("Login.fxml");
            }
        } catch (Exception e) {
            System.err.println("Error returning to previous screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Displays error message
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
    }
    
    /**
     * Displays success message
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
    }
    
    /**
     * Clears all input fields
     */
    private void clearFields() {
        customerNameField.clear();
        emailField.clear();
        phoneNumberField.clear();
        addressField.clear();
        identityNumberField.clear();
        initialDepositField.clear();
        identityTypeComboBox.setValue("National ID");
        accountTypeComboBox.setValue("SAVINGS");
        selectedProfilePicture = null;
        updateProfilePicturePreview(null);
    }
}