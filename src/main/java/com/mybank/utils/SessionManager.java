package com.mybank.utils;

import com.mybank.Main;
import com.mybank.models.Staff;

/**
 * Session Manager
 * Manages user session state across the application
 */
public class SessionManager {
    
    /**
     * Get current customer ID
     * @return Customer ID or -1 if not logged in
     */
    public static int getCurrentCustomerId() {
        // For now, return the logged-in account number as customer ID
        // This assumes the account number corresponds to customer ID
        return Main.getLoggedInAccount();
    }
    
    /**
     * Get current account ID
     * @return Account ID or -1 if not logged in
     */
    public static int getCurrentAccountId() {
        return Main.getLoggedInAccount();
    }
    
    /**
     * Get current staff ID
     * @return Staff ID or -1 if not logged in
     */
    public static int getCurrentStaffId() {
        Staff currentStaff = Main.getCurrentStaff();
        return (currentStaff != null) ? currentStaff.getStaffId() : -1;
    }
    
    /**
     * Get current admin ID
     * @return Admin ID or -1 if not logged in
     */
    public static int getCurrentAdminId() {
        Staff currentStaff = Main.getCurrentStaff();
        if (currentStaff != null && "Admin".equals(currentStaff.getRole())) {
            return currentStaff.getStaffId();
        }
        return -1;
    }
    
    /**
     * Check if customer is logged in
     * @return true if customer logged in
     */
    public static boolean isCustomerLoggedIn() {
        return Main.isLoggedIn();
    }
    
    /**
     * Check if staff is logged in
     * @return true if staff logged in
     */
    public static boolean isStaffLoggedIn() {
        return Main.isStaffLoggedIn();
    }
    
    /**
     * Check if admin is logged in
     * @return true if admin logged in
     */
    public static boolean isAdminLoggedIn() {
        Staff currentStaff = Main.getCurrentStaff();
        return currentStaff != null && "Admin".equals(currentStaff.getRole());
    }
}
