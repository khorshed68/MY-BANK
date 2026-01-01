package com.mybank.models;

import java.time.LocalDateTime;

/**
 * BankConfig model for storing bank-wide configuration and policies.
 */
public class BankConfig {
    private int configId;
    private String configKey;
    private String configValue;
    private String category; // INTEREST_RATE, TRANSACTION_LIMIT, FEE, POLICY, etc.
    private String description;
    private LocalDateTime lastUpdated;
    private String updatedBy;
    
    // Constructors
    public BankConfig() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    public BankConfig(String configKey, String configValue, String category, 
                      String description, String updatedBy) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.category = category;
        this.description = description;
        this.updatedBy = updatedBy;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getConfigId() {
        return configId;
    }
    
    public void setConfigId(int configId) {
        this.configId = configId;
    }
    
    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    
    public String getConfigValue() {
        return configValue;
    }
    
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    @Override
    public String toString() {
        return "BankConfig{" +
                "configId=" + configId +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", category='" + category + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
