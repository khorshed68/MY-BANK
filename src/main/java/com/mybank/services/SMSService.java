package com.mybank.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * SMS Service
 * Handles actual SMS sending using Twilio, AWS SNS, or other SMS providers
 * Provides a unified interface for different SMS backends
 */
public class SMSService {
    
    private final Properties config;
    private final boolean smsEnabled;
    private final boolean debugMode;
    private final String provider;
    
    // Twilio settings
    private String twilioAccountSid;
    private String twilioAuthToken;
    private String twilioPhoneNumber;
    
    public SMSService() {
        this.config = loadConfiguration();
        this.smsEnabled = Boolean.parseBoolean(config.getProperty("sms.enabled", "false"));
        this.debugMode = Boolean.parseBoolean(config.getProperty("notification.debug.mode", "true"));
        this.provider = config.getProperty("sms.provider", "twilio");
        
        // Load provider-specific settings
        if ("twilio".equalsIgnoreCase(provider)) {
            this.twilioAccountSid = config.getProperty("sms.twilio.account.sid");
            this.twilioAuthToken = config.getProperty("sms.twilio.auth.token");
            this.twilioPhoneNumber = config.getProperty("sms.twilio.phone.number");
        }
    }
    
    /**
     * Load SMS configuration from properties file
     */
    private Properties loadConfiguration() {
        Properties props = new Properties();
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("notification.properties");
            if (input != null) {
                props.load(input);
                input.close();
            } else {
                System.err.println("notification.properties file not found in resources folder");
                setDefaultProperties(props);
            }
        } catch (IOException e) {
            System.err.println("Error loading notification.properties: " + e.getMessage());
            setDefaultProperties(props);
        }
        return props;
    }
    
    /**
     * Set default properties
     */
    private void setDefaultProperties(Properties props) {
        props.setProperty("sms.enabled", "false");
        props.setProperty("sms.provider", "twilio");
        props.setProperty("notification.debug.mode", "true");
    }
    
    /**
     * Send SMS to recipient
     * @param phoneNumber The recipient's phone number (format: +1234567890)
     * @param message The SMS message content
     * @return true if SMS was sent successfully, false otherwise
     */
    public boolean sendSMS(String phoneNumber, String message) {
        if (!smsEnabled) {
            if (debugMode) {
                printSimulatedSMS(phoneNumber, message);
            }
            System.err.println("SMS sending is disabled. Enable it in notification.properties");
            return false;
        }
        
        // Validate inputs
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            System.err.println("Cannot send SMS: phone number is empty");
            return false;
        }
        
        if (!isValidPhoneNumber(phoneNumber)) {
            System.err.println("Cannot send SMS: invalid phone number format: " + phoneNumber);
            System.err.println("Phone number should be in format: +1234567890 or +880XXXXXXXXXX");
            return false;
        }
        
        if (message == null || message.trim().isEmpty()) {
            System.err.println("Cannot send SMS: message is empty");
            return false;
        }
        
        // Route to appropriate provider
        try {
            switch (provider.toLowerCase()) {
                case "twilio":
                    return sendViaTwilio(phoneNumber, message);
                case "aws-sns":
                    return sendViaAWSSNS(phoneNumber, message);
                case "africas-talking":
                    return sendViaAfricasTalking(phoneNumber, message);
                default:
                    System.err.println("Unsupported SMS provider: " + provider);
                    if (debugMode) {
                        printSimulatedSMS(phoneNumber, message);
                    }
                    return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            
            if (debugMode) {
                printSimulatedSMS(phoneNumber, message);
            }
            
            return false;
        }
    }
    
    /**
     * Send SMS via Twilio
     */
    private boolean sendViaTwilio(String phoneNumber, String message) {
        // Check if Twilio credentials are configured
        if (twilioAccountSid == null || twilioAuthToken == null || twilioPhoneNumber == null ||
            twilioAccountSid.contains("your-twilio") || twilioAuthToken.contains("your-twilio")) {
            System.err.println("Twilio credentials not configured in notification.properties");
            if (debugMode) {
                printSimulatedSMS(phoneNumber, message);
            }
            return false;
        }
        
        try {
            // Initialize Twilio client
            com.twilio.Twilio.init(twilioAccountSid, twilioAuthToken);
            
            // Send SMS
            com.twilio.rest.api.v2010.account.Message twilioMessage = 
                com.twilio.rest.api.v2010.account.Message.creator(
                    new com.twilio.type.PhoneNumber(phoneNumber),
                    new com.twilio.type.PhoneNumber(twilioPhoneNumber),
                    message
                ).create();
            
            if (debugMode) {
                System.out.println("✅ SMS sent successfully via Twilio to: " + phoneNumber);
                System.out.println("   Message SID: " + twilioMessage.getSid());
            }
            
            return true;
            
        } catch (com.twilio.exception.ApiException e) {
            System.err.println("Twilio API Error: " + e.getMessage());
            if (debugMode) {
                printSimulatedSMS(phoneNumber, message);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error sending SMS via Twilio: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            if (debugMode) {
                printSimulatedSMS(phoneNumber, message);
            }
            return false;
        }
    }
    
    /**
     * Send SMS via AWS SNS
     * TODO: Implement AWS SNS integration
     */
    private boolean sendViaAWSSNS(String phoneNumber, String message) {
        System.err.println("AWS SNS integration not yet implemented");
        if (debugMode) {
            printSimulatedSMS(phoneNumber, message);
        }
        return false;
        
        /*
         * To implement AWS SNS:
         * 1. Add AWS SDK dependency to pom.xml
         * 2. Configure AWS credentials
         * 3. Use AmazonSNS client to publish SMS
         * 
         * Example code:
         * AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient();
         * PublishRequest request = new PublishRequest()
         *     .withMessage(message)
         *     .withPhoneNumber(phoneNumber);
         * snsClient.publish(request);
         */
    }
    
    /**
     * Send SMS via Africa's Talking
     * TODO: Implement Africa's Talking integration
     */
    private boolean sendViaAfricasTalking(String phoneNumber, String message) {
        System.err.println("Africa's Talking integration not yet implemented");
        if (debugMode) {
            printSimulatedSMS(phoneNumber, message);
        }
        return false;
        
        /*
         * To implement Africa's Talking:
         * 1. Add Africa's Talking SDK dependency
         * 2. Configure API credentials
         * 3. Use their SMS API to send messages
         */
    }
    
    /**
     * Validate phone number format
     * Accepts formats: +1234567890, +880XXXXXXXXXX
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove spaces and dashes
        String cleaned = phoneNumber.replaceAll("[\\s-]", "");
        
        // Check if it starts with + and has 10-15 digits
        String phoneRegex = "^\\+[1-9]\\d{9,14}$";
        return cleaned.matches(phoneRegex);
    }
    
    /**
     * Format phone number to international format
     * Converts local Bangladesh numbers to international format
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }
        
        String cleaned = phoneNumber.replaceAll("[\\s-]", "");
        
        // If already in international format, return as is
        if (cleaned.startsWith("+")) {
            return cleaned;
        }
        
        // If Bangladesh number starting with 0, convert to +880
        if (cleaned.startsWith("0") && cleaned.length() == 11) {
            return "+880" + cleaned.substring(1);
        }
        
        // If Bangladesh number without leading 0
        if (cleaned.length() == 10) {
            return "+880" + cleaned;
        }
        
        // Otherwise, assume it needs +880 prefix (adjust for your country)
        return "+" + cleaned;
    }
    
    /**
     * Print simulated SMS to console (for debugging/testing)
     */
    private void printSimulatedSMS(String phoneNumber, String message) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📱 SIMULATED SMS (Real sending disabled)");
        System.out.println("TO: " + phoneNumber);
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println(message);
        System.out.println("═══════════════════════════════════════════════════════\n");
    }
    
    /**
     * Check if SMS service is enabled
     */
    public boolean isSMSEnabled() {
        return smsEnabled;
    }
    
    /**
     * Test SMS configuration by sending a test SMS
     */
    public boolean sendTestSMS(String phoneNumber) {
        String message = "My Bank Test SMS: Your SMS notification system is working correctly.";
        return sendSMS(phoneNumber, message);
    }
}
