package com.mybank.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Email Service
 * Handles actual email sending using JavaMail API and SMTP
 * Supports Gmail, Outlook, and other SMTP providers
 */
public class EmailService {
    
    private final Properties config;
    private final boolean emailEnabled;
    private final boolean debugMode;
    
    public EmailService() {
        this.config = loadConfiguration();
        this.emailEnabled = Boolean.parseBoolean(config.getProperty("email.enabled", "false"));
        this.debugMode = Boolean.parseBoolean(config.getProperty("notification.debug.mode", "true"));
    }
    
    /**
     * Load email configuration from properties file
     */
    private Properties loadConfiguration() {
        Properties props = new Properties();
        try {
            // Try to load from resources folder
            InputStream input = getClass().getClassLoader().getResourceAsStream("notification.properties");
            if (input != null) {
                props.load(input);
                input.close();
            } else {
                System.err.println("notification.properties file not found in resources folder");
                // Set default values
                setDefaultProperties(props);
            }
        } catch (IOException e) {
            System.err.println("Error loading notification.properties: " + e.getMessage());
            setDefaultProperties(props);
        }
        return props;
    }
    
    /**
     * Set default properties when configuration file is not found
     */
    private void setDefaultProperties(Properties props) {
        props.setProperty("email.enabled", "false");
        props.setProperty("email.smtp.host", "smtp.gmail.com");
        props.setProperty("email.smtp.port", "587");
        props.setProperty("email.smtp.auth", "true");
        props.setProperty("email.smtp.starttls.enable", "true");
        props.setProperty("email.from.address", "noreply@mybank.com");
        props.setProperty("email.from.name", "My Bank Notifications");
        props.setProperty("notification.debug.mode", "true");
    }
    
    /**
     * Send email to recipient
     * @param recipientEmail The recipient's email address
     * @param subject The email subject
     * @param messageBody The email body (can be HTML or plain text)
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendEmail(String recipientEmail, String subject, String messageBody) {
        if (!emailEnabled) {
            if (debugMode) {
                printSimulatedEmail(recipientEmail, subject, messageBody);
            }
            System.err.println("Email sending is disabled. Enable it in notification.properties");
            return false;
        }
        
        // Validate inputs
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            System.err.println("Cannot send email: recipient email is empty");
            return false;
        }
        
        if (!isValidEmail(recipientEmail)) {
            System.err.println("Cannot send email: invalid email format: " + recipientEmail);
            return false;
        }
        
        try {
            // Setup mail server properties with enhanced security and timeout settings
            Properties mailProps = new Properties();
            mailProps.put("mail.smtp.host", config.getProperty("email.smtp.host"));
            mailProps.put("mail.smtp.port", config.getProperty("email.smtp.port"));
            mailProps.put("mail.smtp.auth", config.getProperty("email.smtp.auth"));
            mailProps.put("mail.smtp.starttls.enable", config.getProperty("email.smtp.starttls.enable"));
            mailProps.put("mail.smtp.starttls.required", "true");
            
            // SSL/TLS Configuration for Gmail
            mailProps.put("mail.smtp.ssl.trust", "*");
            mailProps.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            // Enable TLS
            mailProps.put("mail.smtp.EnableSSL.enable", "true");
            
            // Timeout settings (30 seconds each)
            mailProps.put("mail.smtp.connectiontimeout", "30000");
            mailProps.put("mail.smtp.timeout", "30000");
            mailProps.put("mail.smtp.writetimeout", "30000");
            
            // Debug mode for troubleshooting
            if (debugMode) {
                mailProps.put("mail.debug", "true");
            }
            
            // Create authenticator
            final String username = config.getProperty("email.username");
            final String password = config.getProperty("email.password");
            
            if (username == null || password == null || 
                username.contains("your-email") || password.contains("your-app-password")) {
                System.err.println("Email credentials not configured in notification.properties");
                if (debugMode) {
                    printSimulatedEmail(recipientEmail, subject, messageBody);
                }
                return false;
            }
            
            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
            
            // Create session
            Session session = Session.getInstance(mailProps, auth);
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(
                config.getProperty("email.from.address"),
                config.getProperty("email.from.name")
            ));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            
            // Set content (HTML or plain text)
            if (messageBody.contains("<html>") || messageBody.contains("<br>")) {
                message.setContent(messageBody, "text/html; charset=utf-8");
            } else {
                message.setText(messageBody);
            }
            
            // Send message
            Transport.send(message);
            
            if (debugMode) {
                System.out.println("✅ Email sent successfully to: " + recipientEmail);
            }
            
            return true;
            
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + recipientEmail + ": " + e.getMessage());
            System.err.println("Error details: " + e.getClass().getName());
            
            if (debugMode) {
                printSimulatedEmail(recipientEmail, subject, messageBody);
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error sending email: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            return false;
        }
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Print simulated email to console (for debugging/testing)
     */
    private void printSimulatedEmail(String recipient, String subject, String message) {
        System.out.println("=======================================================");
        System.out.println(">> SIMULATED EMAIL (Real sending disabled)");
        System.out.println("TO: " + recipient);
        System.out.println("SUBJECT: " + subject);
        System.out.println("-------------------------------------------------------");
        // Remove special characters from message for console display
        String cleanMessage = message.replaceAll("[═─━│┃┏┓┗┛├┤┬┴┼╋]", "-");
        cleanMessage = cleanMessage.replaceAll("[⚠✓✅❌📧📞🏢🎉🔒💬🆔📋📝📎⏱⏰]", "");
        System.out.println(cleanMessage);
        System.out.println("=======================================================\n");
    }
    
    /**
     * Check if email service is enabled
     */
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
    
    /**
     * Test email configuration by sending a test email
     */
    public boolean sendTestEmail(String recipientEmail) {
        String subject = "My Bank - Test Email";
        String message = "This is a test email from My Bank notification system.\n\n" +
                        "If you received this email, your email configuration is working correctly.\n\n" +
                        "Best regards,\nMy Bank Team";
        return sendEmail(recipientEmail, subject, message);
    }
}
