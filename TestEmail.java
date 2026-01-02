import com.mybank.services.EmailService;

public class TestEmail {
    public static void main(String[] args) {
        System.out.println("Testing Email Service...\n");
        
        EmailService emailService = new EmailService();
        
        if (!emailService.isEmailEnabled()) {
            System.err.println("❌ Email is DISABLED in notification.properties");
            System.err.println("Set email.enabled=true to enable email sending");
            return;
        }
        
        System.out.println("Email service is ENABLED");
        System.out.println("Attempting to send test email...\n");
        
        // Replace with a real email address to test
        String testEmail = "sheikhkhorshed1668@gmail.com";
        boolean success = emailService.sendTestEmail(testEmail);
        
        if (success) {
            System.out.println("\n✅ SUCCESS! Email was sent successfully!");
            System.out.println("Check the inbox of: " + testEmail);
        } else {
            System.out.println("\n❌ FAILED! Email could not be sent.");
            System.out.println("Check the error messages above for details.");
        }
    }
}
