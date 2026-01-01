package com.mybank.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;
import com.mybank.models.ActivityLog;
import com.mybank.models.Staff;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for activity log viewer.
 * Allows staff to view and filter activity logs.
 */
public class ActivityLogController {
    
    @FXML
    private ComboBox<String> staffFilter;
    
    @FXML
    private ComboBox<String> actionFilter;
    
    @FXML
    private DatePicker fromDate;
    
    @FXML
    private DatePicker toDate;
    
    @FXML
    private TableView<ActivityLog> activityTable;
    
    @FXML
    private TableColumn<ActivityLog, String> timestampColumn;
    
    @FXML
    private TableColumn<ActivityLog, String> staffNameColumn;
    
    @FXML
    private TableColumn<ActivityLog, String> actionColumn;
    
    @FXML
    private TableColumn<ActivityLog, String> targetAccountColumn;
    
    @FXML
    private TableColumn<ActivityLog, String> detailsColumn;
    
    private Staff currentStaff;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        currentStaff = Main.getCurrentStaff();
        
        // Populate filters
        staffFilter.getItems().addAll("All Staff");
        staffFilter.setValue("All Staff");
        
        actionFilter.getItems().addAll("All Actions", "LOGIN", "APPROVE_ACCOUNT", "REJECT_ACCOUNT", 
                                       "SUSPEND_ACCOUNT", "ACTIVATE_ACCOUNT", "CREATE_STAFF");
        actionFilter.setValue("All Actions");
        
        setupTable();
        loadActivityLogs();
    }
    
    /**
     * Setup table columns
     */
    private void setupTable() {
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTimestamp"));
        staffNameColumn.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        targetAccountColumn.setCellValueFactory(new PropertyValueFactory<>("targetAccountString"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
    }
    
    /**
     * Load activity logs from database
     */
    private void loadActivityLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        
        String sql = "SELECT sal.*, s.fullName as staff_name " +
                     "FROM staff_activity_log sal " +
                     "LEFT JOIN staff s ON sal.staffId = s.staffId " +
                     "WHERE 1=1";
        
        String action = actionFilter.getValue();
        if (action != null && !action.equals("All Actions")) {
            sql += " AND sal.action = ?";
        }
        
        if (fromDate.getValue() != null) {
            sql += " AND DATE(sal.timestamp) >= ?";
        }
        
        if (toDate.getValue() != null) {
            sql += " AND DATE(sal.timestamp) <= ?";
        }
        
        sql += " ORDER BY sal.timestamp DESC LIMIT 1000";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            
            if (action != null && !action.equals("All Actions")) {
                pstmt.setString(paramIndex++, action);
            }
            
            if (fromDate.getValue() != null) {
                pstmt.setString(paramIndex++, fromDate.getValue().toString());
            }
            
            if (toDate.getValue() != null) {
                pstmt.setString(paramIndex++, toDate.getValue().toString());
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("logId"));
                log.setStaffId(rs.getInt("staffId"));
                log.setStaffName(rs.getString("staff_name"));
                log.setAction(rs.getString("action"));
                // Handle null target account
                int targetAcc = rs.getInt("targetAccount");
                if (!rs.wasNull()) {
                    log.setTargetAccount(targetAcc);
                }
                log.setDetails(rs.getString("details"));
                
                // Handle timestamp
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) {
                    log.setTimestamp(ts.toLocalDateTime());
                }
                
                logs.add(log);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading activity logs: " + e.getMessage());
            e.printStackTrace();
        }
        
        ObservableList<ActivityLog> logList = FXCollections.observableArrayList(logs);
        activityTable.setItems(logList);
    }
    
    /**
     * Handle refresh button
     */
    @FXML
    private void handleRefresh() {
        loadActivityLogs();
    }
    
    /**
     * Handle back button
     */
    @FXML
    private void handleBack() {
        Main.showStaffDashboard();
    }
}
