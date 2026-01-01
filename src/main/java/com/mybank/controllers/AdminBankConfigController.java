package com.mybank.controllers;

import java.util.List;
import java.util.Optional;

import com.mybank.Main;
import com.mybank.database.BankConfigDAO;
import com.mybank.models.Admin;
import com.mybank.models.BankConfig;
import com.mybank.services.AdminService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;

/**
 * Controller for Bank Configuration Management.
 */
public class AdminBankConfigController {
    
    @FXML
    private TableView<ConfigRecord> configTable;
    
    @FXML
    private TableColumn<ConfigRecord, String> keyColumn;
    
    @FXML
    private TableColumn<ConfigRecord, String> valueColumn;
    
    @FXML
    private TableColumn<ConfigRecord, String> categoryColumn;
    
    @FXML
    private TableColumn<ConfigRecord, String> descriptionColumn;
    
    @FXML
    private ComboBox<String> categoryFilter;
    
    @FXML
    private Label messageLabel;
    
    private ObservableList<ConfigRecord> configList = FXCollections.observableArrayList();
    private Admin currentAdmin;
    
    @FXML
    private void initialize() {
        currentAdmin = AdminService.getCurrentAdmin();
        
        if (currentAdmin == null) {
            Main.showAdminLogin();
            return;
        }
        
        setupTable();
        setupFilter();
        loadConfigData();
    }
    
    private void setupTable() {
        keyColumn.setCellValueFactory(data -> data.getValue().keyProperty());
        valueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());
    }
    
    private void setupFilter() {
        categoryFilter.setItems(FXCollections.observableArrayList(
            "All", "INTEREST_RATE", "BALANCE", "TRANSACTION_LIMIT", "FEE", "PENALTY", "SECURITY"
        ));
        categoryFilter.setValue("All");
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterData());
    }
    
    private void loadConfigData() {
        configList.clear();
        List<BankConfig> configs = BankConfigDAO.getAllConfigs();
        
        for (BankConfig config : configs) {
            configList.add(new ConfigRecord(
                config.getConfigKey(),
                config.getConfigValue(),
                config.getCategory(),
                config.getDescription()
            ));
        }
        
        configTable.setItems(configList);
    }
    
    private void filterData() {
        String filter = categoryFilter.getValue();
        
        if (filter.equals("All")) {
            configTable.setItems(configList);
        } else {
            ObservableList<ConfigRecord> filtered = FXCollections.observableArrayList();
            for (ConfigRecord config : configList) {
                if (config.getCategory().equals(filter)) {
                    filtered.add(config);
                }
            }
            configTable.setItems(filtered);
        }
    }
    
    @FXML
    private void handleUpdateValue() {
        ConfigRecord selected = configTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a configuration");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selected.getValue());
        dialog.setTitle("Update Configuration");
        dialog.setHeaderText("Update: " + selected.getKey());
        dialog.setContentText("New Value:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newValue = result.get().trim();
            
            if (newValue.isEmpty()) {
                showError("Value cannot be empty");
                return;
            }
            
            boolean success = BankConfigDAO.updateConfig(selected.getKey(), newValue, currentAdmin.getUsername());
            
            if (success) {
                AdminService.logAuditEvent("ADMIN", currentAdmin.getAdminId(), currentAdmin.getUsername(),
                    "UPDATE", "CONFIG", "Updated config: " + selected.getKey() + " to " + newValue, "SUCCESS");
                
                showSuccess("Configuration updated successfully");
                loadConfigData();
            } else {
                showError("Failed to update configuration");
            }
        }
    }
    
    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
    
    public static class ConfigRecord {
        private final SimpleStringProperty key;
        private final SimpleStringProperty value;
        private final SimpleStringProperty category;
        private final SimpleStringProperty description;
        
        public ConfigRecord(String key, String value, String category, String description) {
            this.key = new SimpleStringProperty(key);
            this.value = new SimpleStringProperty(value);
            this.category = new SimpleStringProperty(category);
            this.description = new SimpleStringProperty(description);
        }
        
        public String getKey() { return key.get(); }
        public String getValue() { return value.get(); }
        public String getCategory() { return category.get(); }
        public String getDescription() { return description.get(); }
        
        public SimpleStringProperty keyProperty() { return key; }
        public SimpleStringProperty valueProperty() { return value; }
        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty descriptionProperty() { return description; }
    }
}
