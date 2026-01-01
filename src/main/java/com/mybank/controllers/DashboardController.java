package com.mybank.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.services.NotificationService;
import com.mybank.utils.ProfilePictureManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Dashboard Controller
 * Handles navigation from the main dashboard
 */
public class DashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private ImageView profilePictureView;
    
    private NotificationService notificationService;
    private DatabaseHelper dbHelper;
    
    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        notificationService = new NotificationService();
        dbHelper = new DatabaseHelper();
        updateWelcomeMessage();
        loadProfilePicture();
    }
    
    /**
     * Loads profile picture for logged-in user
     * Automatically centers and crops any image to fit the circular shape
     */
    private void loadProfilePicture() {
        try {
            int accountNumber = Main.getLoggedInAccount();
            if (accountNumber > 0) {
                String profilePicturePath = getProfilePicturePath(accountNumber);
                Image profileImage = ProfilePictureManager.loadProfilePicture(profilePicturePath);
                
                if (profileImage != null) {
                    profilePictureView.setImage(profileImage);
                    
                    // Calculate viewport to center the image
                    double imageWidth = profileImage.getWidth();
                    double imageHeight = profileImage.getHeight();
                    double size = Math.min(imageWidth, imageHeight);
                    double offsetX = (imageWidth - size) / 2;
                    double offsetY = (imageHeight - size) / 2;
                    
                    // Set viewport to show center square portion
                    profilePictureView.setViewport(new javafx.geometry.Rectangle2D(offsetX, offsetY, size, size));
                    
                    // Set proper display settings for circular image
                    profilePictureView.setPreserveRatio(false);
                    profilePictureView.setFitWidth(132);
                    profilePictureView.setFitHeight(132);
                    profilePictureView.setSmooth(true);
                } else {
                    profilePictureView.setImage(ProfilePictureManager.loadDefaultAvatar());
                }
            } else {
                profilePictureView.setImage(ProfilePictureManager.loadDefaultAvatar());
            }
        } catch (Exception e) {
            System.err.println("Error loading profile picture: " + e.getMessage());
            profilePictureView.setImage(ProfilePictureManager.loadDefaultAvatar());
        }
    }
    
    /**
     * Get profile picture path from database
     */
    private String getProfilePicturePath(int accountNumber) {
        String query = "SELECT profilePicturePath FROM accounts WHERE accountNumber = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("profilePicturePath");
            }
        } catch (Exception e) {
            System.err.println("Error fetching profile picture path: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Updates the welcome message with customer name
     */
    private void updateWelcomeMessage() {
        try {
            int accountNumber = Main.getLoggedInAccount();
            if (accountNumber > 0) {
                String ownerName = dbHelper.getOwnerName(accountNumber);
                if (ownerName != null && !ownerName.isEmpty()) {
                    welcomeLabel.setText("Welcome, " + ownerName + " (Account: " + accountNumber + ")");
                } else {
                    welcomeLabel.setText("Welcome, Customer (Account: " + accountNumber + ")");
                }
            } else {
                welcomeLabel.setText("Welcome, Customer");
            }
        } catch (Exception e) {
            welcomeLabel.setText("Welcome, Customer");
            System.err.println("Error loading welcome message: " + e.getMessage());
        }
    }
    
    /**
     * Opens Create Account page
     */
    @FXML
    private void openCreateAccount() {
        try {
            Main.changeScene("CreateAccount.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Create Account: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Deposit page
     */
    @FXML
    private void openDeposit() {
        try {
            Main.changeScene("Deposit.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Deposit: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Withdraw page
     */
    @FXML
    private void openWithdraw() {
        try {
            Main.changeScene("Withdraw.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Withdraw: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Transfer page
     */
    @FXML
    private void openTransfer() {
        try {
            Main.changeScene("Transfer.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Check Balance page
     */
    @FXML
    private void openCheckBalance() {
        try {
            Main.changeScene("CheckBalance.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Check Balance: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Account Overview page
     */
    @FXML
    private void openAccountOverview() {
        try {
            Main.changeScene("AccountOverview.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Account Overview: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Transaction History page
     */
    @FXML
    private void openTransactionHistory() {
        try {
            Main.changeScene("TransactionHistory.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Transaction History: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Notification Settings page
     */
    @FXML
    private void openNotificationSettings() {
        try {
            Main.changeScene("NotificationSettings.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Notification Settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Profile Settings page
     */
    @FXML
    private void openProfileSettings() {
        try {
            Main.changeScene("ProfileSettings.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Profile Settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Notification History page
     */
    @FXML
    private void openNotificationHistory() {
        try {
            Main.changeScene("NotificationHistory.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Notification History: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens Cheque Management page
     */
    @FXML
    private void openChequeManagement() {
        try {
            Main.changeScene("CustomerCheque.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Cheque Management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Logs out and returns to login screen
     */
    @FXML
    private void logout() {
        try {
            int loggedInAccount = Main.getLoggedInAccount();
            
            // Send logout notification before clearing session
            if (loggedInAccount > 0) {
                new Thread(() -> {
                    notificationService.sendLogoutNotification(loggedInAccount);
                }).start();
            }
            
            Main.clearLoggedInAccount();
            Main.changeScene("Login.fxml");
        } catch (Exception e) {
            System.err.println("Error logging out: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Exits the application
     */
    @FXML
    private void exitApplication() {
        Platform.exit();
        System.exit(0);
    }
}
