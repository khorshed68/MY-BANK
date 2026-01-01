package com.mybank.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mybank.Main;
import com.mybank.database.DatabaseHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for viewing notification history
 */
public class NotificationHistoryController {
    
    @FXML private TableView<NotificationRecord> notificationTable;
    @FXML private TableColumn<NotificationRecord, String> timestampColumn;
    @FXML private TableColumn<NotificationRecord, String> typeColumn;
    @FXML private TableColumn<NotificationRecord, String> channelColumn;
    @FXML private TableColumn<NotificationRecord, String> recipientColumn;
    @FXML private TableColumn<NotificationRecord, String> statusColumn;
    @FXML private TextArea messageTextArea;
    @FXML private Label statusLabel;
    
    private DatabaseHelper dbHelper;
    private int accountNumber;
    
    @FXML
    public void initialize() {
        dbHelper = new DatabaseHelper();
        accountNumber = Main.getLoggedInAccount();
        
        // Setup table columns
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        channelColumn.setCellValueFactory(new PropertyValueFactory<>("channel"));
        recipientColumn.setCellValueFactory(new PropertyValueFactory<>("recipient"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Load notification history
        loadNotificationHistory();
        
        // Add selection listener to show message details
        notificationTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    messageTextArea.setText(newSelection.getMessage());
                }
            }
        );
    }
    
    private void loadNotificationHistory() {
        ObservableList<NotificationRecord> notifications = FXCollections.observableArrayList();
        
        String sql = "SELECT notificationType, channel, recipient, message, status, sentTimestamp " +
                     "FROM notifications_log WHERE accountNumber = ? ORDER BY id DESC LIMIT 50";
        
        try {
            Connection conn = dbHelper.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                notifications.add(new NotificationRecord(
                    rs.getString("sentTimestamp"),
                    rs.getString("notificationType"),
                    rs.getString("channel"),
                    rs.getString("recipient"),
                    rs.getString("status"),
                    rs.getString("message")
                ));
                count++;
            }
            
            notificationTable.setItems(notifications);
            statusLabel.setText("Total Notifications: " + count);
            statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading notifications: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #c62828;");
        }
    }
    
    @FXML
    private void backToDashboard() {
        try {
            Main.changeScene("Dashboard.fxml");
        } catch (Exception e) {
            System.err.println("Error returning to dashboard: " + e.getMessage());
        }
    }
    
    @FXML
    private void refreshHistory() {
        loadNotificationHistory();
        messageTextArea.clear();
    }
    
    /**
     * Inner class for notification records
     */
    public static class NotificationRecord {
        private String timestamp;
        private String type;
        private String channel;
        private String recipient;
        private String status;
        private String message;
        
        public NotificationRecord(String timestamp, String type, String channel, 
                                 String recipient, String status, String message) {
            this.timestamp = timestamp;
            this.type = type;
            this.channel = channel;
            this.recipient = recipient;
            this.status = status;
            this.message = message;
        }
        
        public String getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public String getChannel() { return channel; }
        public String getRecipient() { return recipient; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }
}
