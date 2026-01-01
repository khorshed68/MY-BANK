package com.mybank.controllers;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.mybank.Main;
import com.mybank.models.Admin;
import com.mybank.models.AuditLog;
import com.mybank.services.AdminService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controller for Audit Logs viewing.
 */
public class AdminAuditLogsController {
    
    @FXML
    private TableView<AuditRecord> auditTable;
    
    @FXML
    private TableColumn<AuditRecord, String> timestampColumn;
    
    @FXML
    private TableColumn<AuditRecord, String> userTypeColumn;
    
    @FXML
    private TableColumn<AuditRecord, String> usernameColumn;
    
    @FXML
    private TableColumn<AuditRecord, String> actionColumn;
    
    @FXML
    private TableColumn<AuditRecord, String> moduleColumn;
    
    @FXML
    private TableColumn<AuditRecord, String> detailsColumn;
    
    @FXML
    private TableColumn<AuditRecord, String> statusColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> filterComboBox;
    
    @FXML
    private Label messageLabel;
    
    private ObservableList<AuditRecord> auditList = FXCollections.observableArrayList();
    private Admin currentAdmin;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @FXML
    private void initialize() {
        currentAdmin = AdminService.getCurrentAdmin();
        
        if (currentAdmin == null) {
            Main.showAdminLogin();
            return;
        }
        
        setupTable();
        setupFilters();
        loadAuditData();
    }
    
    private void setupTable() {
        timestampColumn.setCellValueFactory(data -> data.getValue().timestampProperty());
        userTypeColumn.setCellValueFactory(data -> data.getValue().userTypeProperty());
        usernameColumn.setCellValueFactory(data -> data.getValue().usernameProperty());
        actionColumn.setCellValueFactory(data -> data.getValue().actionProperty());
        moduleColumn.setCellValueFactory(data -> data.getValue().moduleProperty());
        detailsColumn.setCellValueFactory(data -> data.getValue().detailsProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
    }
    
    private void setupFilters() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All", "ADMIN", "STAFF", "CUSTOMER", "LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", "SUCCESS", "FAILED"
        ));
        filterComboBox.setValue("All");
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterData());
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterData());
    }
    
    private void loadAuditData() {
        auditList.clear();
        List<AuditLog> logs = AdminService.getAuditLogs();
        
        if (logs != null) {
            for (AuditLog log : logs) {
                auditList.add(new AuditRecord(
                    log.getTimestamp().format(formatter),
                    log.getUserType(),
                    log.getUsername(),
                    log.getAction(),
                    log.getModule(),
                    log.getDetails(),
                    log.getStatus()
                ));
            }
        }
        
        auditTable.setItems(auditList);
    }
    
    private void filterData() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filter = filterComboBox.getValue();
        
        ObservableList<AuditRecord> filtered = FXCollections.observableArrayList();
        
        for (AuditRecord audit : auditList) {
            boolean matchesSearch = searchText.isEmpty() || 
                audit.getUsername().toLowerCase().contains(searchText) ||
                audit.getDetails().toLowerCase().contains(searchText);
            
            boolean matchesFilter = filter.equals("All") ||
                audit.getUserType().equals(filter) ||
                audit.getAction().equals(filter) ||
                audit.getStatus().equals(filter);
            
            if (matchesSearch && matchesFilter) {
                filtered.add(audit);
            }
        }
        
        auditTable.setItems(filtered);
    }
    
    @FXML
    private void handleRefresh() {
        loadAuditData();
    }
    
    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }
    
    public static class AuditRecord {
        private final SimpleStringProperty timestamp;
        private final SimpleStringProperty userType;
        private final SimpleStringProperty username;
        private final SimpleStringProperty action;
        private final SimpleStringProperty module;
        private final SimpleStringProperty details;
        private final SimpleStringProperty status;
        
        public AuditRecord(String timestamp, String userType, String username, String action,
                          String module, String details, String status) {
            this.timestamp = new SimpleStringProperty(timestamp);
            this.userType = new SimpleStringProperty(userType);
            this.username = new SimpleStringProperty(username);
            this.action = new SimpleStringProperty(action);
            this.module = new SimpleStringProperty(module);
            this.details = new SimpleStringProperty(details);
            this.status = new SimpleStringProperty(status);
        }
        
        public String getTimestamp() { return timestamp.get(); }
        public String getUserType() { return userType.get(); }
        public String getUsername() { return username.get(); }
        public String getAction() { return action.get(); }
        public String getModule() { return module.get(); }
        public String getDetails() { return details.get(); }
        public String getStatus() { return status.get(); }
        
        public SimpleStringProperty timestampProperty() { return timestamp; }
        public SimpleStringProperty userTypeProperty() { return userType; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty actionProperty() { return action; }
        public SimpleStringProperty moduleProperty() { return module; }
        public SimpleStringProperty detailsProperty() { return details; }
        public SimpleStringProperty statusProperty() { return status; }
    }
}
