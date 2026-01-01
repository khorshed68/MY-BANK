module com.mybank {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;  // Required for image processing (BufferedImage, Graphics2D, ImageIO)
    
    // Email and SMS notification modules
    requires java.mail;
    requires twilio;

    opens com.mybank to javafx.fxml;
    opens com.mybank.controllers to javafx.fxml;
    opens com.mybank.models to javafx.base;
    
    exports com.mybank;
    exports com.mybank.controllers;
    exports com.mybank.models;
    exports com.mybank.database;
}
