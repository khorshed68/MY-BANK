package com.mybank.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Utility class for managing profile picture uploads
 * Handles validation, storage, and retrieval of user profile pictures
 */
public class ProfilePictureManager {
    
    // Configuration Constants
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB
    private static final int MAX_IMAGE_WIDTH = 500;
    private static final int MAX_IMAGE_HEIGHT = 500;
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png"};
    private static final String PROFILE_PICTURES_DIR = "profile_pictures";
    private static final String DEFAULT_AVATAR_PATH = "/images/default-avatar.png";
    
    /**
     * Open file chooser dialog for profile picture selection
     * @return Selected file or null if cancelled
     */
    public static File openFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        
        // Set file extension filters
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
            "Image Files (*.jpg, *.jpeg, *.png)", "*.jpg", "*.jpeg", "*.png", "*.JPG", "*.JPEG", "*.PNG"
        );
        fileChooser.getExtensionFilters().add(imageFilter);
        
        return fileChooser.showOpenDialog(stage);
    }
    
    /**
     * Validate uploaded image file
     * @param file File to validate
     * @return ValidationResult with success status and message
     */
    public static ValidationResult validateImage(File file) {
        if (file == null) {
            return new ValidationResult(false, "No file selected");
        }
        
        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            double sizeMB = file.length() / (1024.0 * 1024.0);
            return new ValidationResult(false, 
                String.format("File size (%.2f MB) exceeds maximum allowed size (2 MB)", sizeMB));
        }
        
        // Check file extension
        String fileName = file.getName().toLowerCase();
        boolean validExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (fileName.endsWith("." + ext)) {
                validExtension = true;
                break;
            }
        }
        
        if (!validExtension) {
            return new ValidationResult(false, 
                "Invalid file type. Only JPG, JPEG, and PNG files are allowed.");
        }
        
        // Try to read the image to verify it's a valid image file
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                return new ValidationResult(false, "Invalid image file");
            }
        } catch (IOException e) {
            return new ValidationResult(false, "Cannot read image file: " + e.getMessage());
        }
        
        return new ValidationResult(true, "Valid image file");
    }
    
    /**
     * Save profile picture to storage
     * @param sourceFile Source image file
     * @param accountNumber Account number (used for filename)
     * @return Path to saved file or null if failed
     */
    public static String saveProfilePicture(File sourceFile, int accountNumber) throws IOException {
        // Create profile pictures directory if it doesn't exist
        Path profilePicturesPath = Paths.get(PROFILE_PICTURES_DIR);
        if (!Files.exists(profilePicturesPath)) {
            Files.createDirectories(profilePicturesPath);
        }
        
        // Generate unique filename
        String extension = getFileExtension(sourceFile.getName());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = "profile_" + accountNumber + "_" + timestamp + "." + extension;
        Path destinationPath = profilePicturesPath.resolve(filename);
        
        // Resize and save image
        BufferedImage originalImage = ImageIO.read(sourceFile);
        BufferedImage resizedImage = resizeImage(originalImage, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
        
        // Save resized image
        ImageIO.write(resizedImage, extension, destinationPath.toFile());
        
        return destinationPath.toString();
    }
    
    /**
     * Save profile picture for account request (temporary storage)
     * @param sourceFile Source image file
     * @param requestId Request ID (used for filename)
     * @return Path to saved file or null if failed
     */
    public static String saveProfilePictureForRequest(File sourceFile, String requestId) throws IOException {
        // Create profile pictures directory if it doesn't exist
        Path profilePicturesPath = Paths.get(PROFILE_PICTURES_DIR, "requests");
        if (!Files.exists(profilePicturesPath)) {
            Files.createDirectories(profilePicturesPath);
        }
        
        // Generate unique filename
        String extension = getFileExtension(sourceFile.getName());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = "request_" + requestId + "_" + timestamp + "." + extension;
        Path destinationPath = profilePicturesPath.resolve(filename);
        
        // Resize and save image
        BufferedImage originalImage = ImageIO.read(sourceFile);
        BufferedImage resizedImage = resizeImage(originalImage, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
        
        // Save resized image
        ImageIO.write(resizedImage, extension, destinationPath.toFile());
        
        return destinationPath.toString();
    }
    
    /**
     * Resize image while maintaining aspect ratio
     * @param originalImage Original image
     * @param maxWidth Maximum width
     * @param maxHeight Maximum height
     * @return Resized image
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate new dimensions while maintaining aspect ratio
        int newWidth = originalWidth;
        int newHeight = originalHeight;
        
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            double widthRatio = (double) maxWidth / originalWidth;
            double heightRatio = (double) maxHeight / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);
            
            newWidth = (int) (originalWidth * ratio);
            newHeight = (int) (originalHeight * ratio);
        }
        
        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        
        // Set rendering hints for better quality
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return resizedImage;
    }
    
    /**
     * Get file extension from filename
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Load profile picture as JavaFX Image
     * @param profilePicturePath Path to profile picture
     * @return JavaFX Image or default avatar if path is null/invalid
     */
    public static Image loadProfilePicture(String profilePicturePath) {
        if (profilePicturePath == null || profilePicturePath.isEmpty()) {
            return loadDefaultAvatar();
        }
        
        try {
            File imageFile = new File(profilePicturePath);
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString(), 150, 150, true, true);
            }
        } catch (Exception e) {
            System.err.println("Error loading profile picture: " + e.getMessage());
        }
        
        return loadDefaultAvatar();
    }
    
    /**
     * Load default avatar image
     * @return Default avatar image
     */
    public static Image loadDefaultAvatar() {
        try {
            // Try to load from resources
            var stream = ProfilePictureManager.class.getResourceAsStream(DEFAULT_AVATAR_PATH);
            if (stream != null) {
                return new Image(stream, 150, 150, true, true);
            }
        } catch (Exception e) {
            System.err.println("Default avatar resource not found, using generated avatar");
        }
        
        // Generate a simple default avatar programmatically
        return createDefaultAvatarImage();
    }
    
    /**
     * Create a default avatar image programmatically
     * @return Generated default avatar image
     */
    private static Image createDefaultAvatarImage() {
        try {
            // Create a simple circular avatar with user icon
            int size = 150;
            BufferedImage avatar = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = avatar.createGraphics();
            
            // Enable anti-aliasing for smooth edges
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Fill with gradient background (light blue to purple)
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                0, 0, new java.awt.Color(103, 126, 234),
                size, size, new java.awt.Color(118, 75, 162)
            );
            g2d.setPaint(gradient);
            g2d.fillOval(0, 0, size, size);
            
            // Draw user icon (head and shoulders silhouette)
            g2d.setColor(java.awt.Color.WHITE);
            
            // Draw head (circle)
            int headSize = size / 3;
            int headX = (size - headSize) / 2;
            int headY = size / 4;
            g2d.fillOval(headX, headY, headSize, headSize);
            
            // Draw shoulders (arc)
            int shoulderWidth = (int)(size * 0.7);
            int shoulderHeight = size / 3;
            int shoulderX = (size - shoulderWidth) / 2;
            int shoulderY = (int)(size * 0.6);
            g2d.fillArc(shoulderX, shoulderY, shoulderWidth, shoulderHeight, 0, 180);
            
            g2d.dispose();
            
            // Convert BufferedImage to JavaFX Image
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(avatar, "png", baos);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
            
            return new Image(bais, 150, 150, true, true);
            
        } catch (Exception e) {
            System.err.println("Error creating default avatar: " + e.getMessage());
            e.printStackTrace();
            
            // Return a minimal fallback
            return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
        }
    }
    
    /**
     * Delete profile picture file
     * @param profilePicturePath Path to profile picture
     */
    public static void deleteProfilePicture(String profilePicturePath) {
        if (profilePicturePath == null || profilePicturePath.isEmpty()) {
            return;
        }
        
        try {
            File file = new File(profilePicturePath);
            if (file.exists()) {
                Files.delete(file.toPath());
            }
        } catch (IOException e) {
            System.err.println("Error deleting profile picture: " + e.getMessage());
        }
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
