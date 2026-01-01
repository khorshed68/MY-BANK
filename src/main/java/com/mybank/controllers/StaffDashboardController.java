package com.mybank.controllers;

import com.mybank.Main;
import com.mybank.models.Staff;
import com.mybank.utils.ProfilePictureManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controller for staff dashboard.
 * Displays role-based menu options and navigation.
 */
public class StaffDashboardController {
    
    @FXML
    private Label staffInfoLabel;
    
    @FXML
    private ImageView profilePictureView;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private Label lastLoginLabel;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private VBox accountManagementSection;
    
    @FXML
    private VBox reportsSection;
    
    @FXML
    private VBox adminSection;
    
    @FXML
    private Button viewPendingRequestsBtn;
    
    @FXML
    private Button createAccountBtn;
    
    @FXML
    private Button viewCustomersBtn;
    
    @FXML
    private Button generateReportBtn;
    
    @FXML
    private Button viewActivityLogBtn;
    
    @FXML
    private Button manageStaffBtn;
    
    @FXML
    private Button systemSettingsBtn;
    
    private Staff currentStaff;
    
    /**
     * Initialize the dashboard
     */
    @FXML
    private void initialize() {
        currentStaff = Main.getCurrentStaff();
        
        if (currentStaff != null) {
            loadProfilePicture();
            loadStaffInfo();
            configureRoleBasedAccess();
        }
    }
    
    /**
     * Load staff information
     */
    private void loadStaffInfo() {
        welcomeLabel.setText("Welcome, " + currentStaff.getFullName() + "!");
        staffInfoLabel.setText(currentStaff.getFullName() + " • " + currentStaff.getEmail());
        roleLabel.setText(currentStaff.getRoleDisplay());
        
        if (currentStaff.getLastLogin() != null) {
            lastLoginLabel.setText("Last Login: " + currentStaff.getFormattedLastLogin());
        } else {
            lastLoginLabel.setText("First Login");
        }
    }
    
    /**
     * Load staff profile picture
     * Automatically centers and crops any image to fit the circular shape
     */
    private void loadProfilePicture() {
        try {
            String profilePicturePath = currentStaff.getProfilePicturePath();
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
                profilePictureView.setFitWidth(74);
                profilePictureView.setFitHeight(74);
                profilePictureView.setSmooth(true);
            } else {
                profilePictureView.setImage(ProfilePictureManager.loadDefaultAvatar());
            }
        } catch (Exception e) {
            System.err.println("Error loading staff profile picture: " + e.getMessage());
            profilePictureView.setImage(ProfilePictureManager.loadDefaultAvatar());
        }
    }
    
    /**
     * Configure menu visibility based on staff role
     */
    private void configureRoleBasedAccess() {
        String role = currentStaff.getRole();
        
        // Admin has full access
        if (Staff.ROLE_ADMIN.equals(role)) {
            adminSection.setVisible(true);
            adminSection.setManaged(true);
            enableAllButtons();
        }
        // Manager has most access except staff management
        else if (Staff.ROLE_MANAGER.equals(role)) {
            adminSection.setVisible(false);
            adminSection.setManaged(false);
            enableAllButtons();
        }
        // Officer has limited access
        else if (Staff.ROLE_OFFICER.equals(role)) {
            adminSection.setVisible(false);
            adminSection.setManaged(false);
            generateReportBtn.setDisable(true);
            createAccountBtn.setDisable(true);
        }
        // Teller has basic access
        else if (Staff.ROLE_TELLER.equals(role)) {
            adminSection.setVisible(false);
            adminSection.setManaged(false);
            generateReportBtn.setDisable(true);
            viewPendingRequestsBtn.setDisable(true);
            createAccountBtn.setDisable(true);
            viewActivityLogBtn.setDisable(true);
        }
    }
    
    /**
     * Enable all menu buttons
     */
    private void enableAllButtons() {
        viewPendingRequestsBtn.setDisable(false);
        createAccountBtn.setDisable(false);
        viewCustomersBtn.setDisable(false);
        generateReportBtn.setDisable(false);
        viewActivityLogBtn.setDisable(false);
    }
    
    /**
     * Handle view pending requests
     */
    @FXML
    private void handleViewPendingRequests() {
        Main.showAccountApproval();
    }
    
    /**
     * Handle create account
     */
    @FXML
    private void handleCreateAccount() {
        Main.showCreateAccountRequest();
    }
    
    /**
     * Handle view customers
     */
    @FXML
    private void handleViewCustomers() {
        Main.showCustomerManagement();
    }
    
    /**
     * Handle generate report
     */
    @FXML
    private void handleGenerateReport() {
        Main.showReportGeneration();
    }
    
    /**
     * Handle view activity log
     */
    @FXML
    private void handleViewActivityLog() {
        Main.showActivityLog();
    }
    
    /**
     * Handle manage staff (Admin only)
     */
    @FXML
    private void handleManageStaff() {
        Main.showStaffManagement();
    }
    
    /**
     * Handle system settings (Admin only)
     */
    @FXML
    private void handleSystemSettings() {
        System.out.println("System settings - Coming soon");
    }
    
    /**
     * Handle manage cheques
     */
    @FXML
    private void handleManageCheques() {
        try {
            Main.changeScene("StaffChequeManagement.fxml");
        } catch (Exception e) {
            System.err.println("Error opening Cheque Management: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle refresh dashboard
     */
    @FXML
    private void handleRefresh() {
        initialize();
        System.out.println("Dashboard refreshed");
    }
    
    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        Main.setCurrentStaff(null);
        Main.showStaffLogin();
    }
}
