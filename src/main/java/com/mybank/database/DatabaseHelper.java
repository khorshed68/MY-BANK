package com.mybank.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DatabaseHelper - Manages all SQLite database operations
 * Provides methods for account and transaction management with authentication
 */
public class DatabaseHelper {
    
    private static final String DB_URL = "jdbc:sqlite:database/mybank.db";
    private static Connection sharedConnection;
    private static final Object connectionLock = new Object();
    
    // Default password for new accounts (to be changed on first login)
    public static final String DEFAULT_PASSWORD = "bank@123";
    
    /**
     * Constructor - Initializes database connection and creates tables
     */
    public DatabaseHelper() {
        try {
            synchronized (connectionLock) {
                if (sharedConnection == null || sharedConnection.isClosed()) {
                    // Load SQLite JDBC driver
                    Class.forName("org.sqlite.JDBC");
                    
                    // Enable shared cache and WAL mode for better concurrency
                    sharedConnection = DriverManager.getConnection(DB_URL + "?journal_mode=WAL");
                    
                    // Set busy timeout to 5 seconds (5000 ms)
                    Statement stmt = sharedConnection.createStatement();
                    stmt.execute("PRAGMA busy_timeout = 5000");
                    stmt.execute("PRAGMA journal_mode = WAL");
                    stmt.execute("PRAGMA synchronous = NORMAL");
                    stmt.close();
                    
                    createTables();
                    System.out.println("Database connected successfully with WAL mode!");
                }
            }
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ensures the shared connection is open
     */
    private static void ensureConnection() {
        synchronized (connectionLock) {
            try {
                if (sharedConnection == null || sharedConnection.isClosed()) {
                    sharedConnection = DriverManager.getConnection(DB_URL + "?journal_mode=WAL");
                    Statement stmt = sharedConnection.createStatement();
                    stmt.execute("PRAGMA busy_timeout = 5000");
                    stmt.execute("PRAGMA journal_mode = WAL");
                    stmt.execute("PRAGMA synchronous = NORMAL");
                    stmt.close();
                }
            } catch (SQLException e) {
                System.err.println("Error ensuring connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Gets the database connection
     * @return Connection object
     */
    public static Connection getConnection() {
        ensureConnection();
        return sharedConnection;
    }
    
    /**
     * Creates database tables if they don't exist
     */
    private void createTables() {
        try {
            Statement stmt = sharedConnection.createStatement();
            
            // Create accounts table with authentication and notification fields
            String accountsTable = "CREATE TABLE IF NOT EXISTS accounts (" +
                    "accountNumber INTEGER PRIMARY KEY, " +
                    "ownerName TEXT NOT NULL, " +
                    "passwordHash TEXT NOT NULL DEFAULT '', " +
                    "balance REAL NOT NULL DEFAULT 0, " +
                    "status TEXT NOT NULL DEFAULT 'ACTIVE', " +
                    "failedAttempts INTEGER NOT NULL DEFAULT 0, " +
                    "email TEXT, " +
                    "phoneNumber TEXT, " +
                    "smsEnabled INTEGER DEFAULT 1, " +
                    "emailEnabled INTEGER DEFAULT 1, " +
                    "accountType TEXT DEFAULT 'SAVINGS', " +
                    "availableBalance REAL DEFAULT 0, " +
                    "lastTransactionDate DATETIME, " +
                    "createdDate DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(accountsTable);
            
            // Add missing columns to existing accounts (for upgrade compatibility)
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN passwordHash TEXT NOT NULL DEFAULT ''");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN failedAttempts INTEGER NOT NULL DEFAULT 0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN email TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN phoneNumber TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN smsEnabled INTEGER DEFAULT 1");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN emailEnabled INTEGER DEFAULT 1");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN accountType TEXT DEFAULT 'SAVINGS'");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN availableBalance REAL DEFAULT 0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN lastTransactionDate DATETIME");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN createdDate DATETIME DEFAULT CURRENT_TIMESTAMP");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            // Create transactions table
            String transactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "accountNumber INTEGER NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (accountNumber) REFERENCES accounts(accountNumber))";
            stmt.execute(transactionsTable);
            
            // Create notifications_log table
            String notificationsTable = "CREATE TABLE IF NOT EXISTS notifications_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "accountNumber INTEGER NOT NULL, " +
                    "notificationType TEXT NOT NULL, " +
                    "channel TEXT NOT NULL, " +
                    "recipient TEXT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "sentTimestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "eventDetails TEXT, " +
                    "FOREIGN KEY (accountNumber) REFERENCES accounts(accountNumber))";
            stmt.execute(notificationsTable);
            
            // Create loans table for loan account management
            String loansTable = "CREATE TABLE IF NOT EXISTS loans (" +
                    "loanId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "accountNumber INTEGER NOT NULL, " +
                    "loanAmount REAL NOT NULL, " +
                    "outstandingBalance REAL NOT NULL, " +
                    "interestRate REAL NOT NULL, " +
                    "installmentAmount REAL NOT NULL, " +
                    "nextDueDate DATE, " +
                    "repaymentStatus TEXT DEFAULT 'ACTIVE', " +
                    "loanTerm INTEGER NOT NULL, " +
                    "disbursementDate DATE DEFAULT CURRENT_DATE, " +
                    "lastPaymentDate DATE, " +
                    "FOREIGN KEY (accountNumber) REFERENCES accounts(accountNumber))";
            stmt.execute(loansTable);
            
            // Create staff table for bank staff management
            String staffTable = "CREATE TABLE IF NOT EXISTS staff (" +
                    "staffId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "passwordHash TEXT NOT NULL, " +
                    "fullName TEXT NOT NULL, " +
                    "email TEXT, " +
                    "phoneNumber TEXT, " +
                    "role TEXT NOT NULL, " +
                    "status TEXT DEFAULT 'ACTIVE', " +
                    "createdDate DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "lastLogin DATETIME)";
            stmt.execute(staffTable);
            
            // Create account_requests table for pending account applications
            String accountRequestsTable = "CREATE TABLE IF NOT EXISTS account_requests (" +
                    "requestId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "customerName TEXT NOT NULL, " +
                    "email TEXT, " +
                    "phoneNumber TEXT NOT NULL, " +
                    "address TEXT, " +
                    "identityType TEXT NOT NULL, " +
                    "identityNumber TEXT NOT NULL, " +
                    "accountType TEXT NOT NULL, " +
                    "initialDeposit REAL DEFAULT 0, " +
                    "requestStatus TEXT DEFAULT 'PENDING', " +
                    "requestDate DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "processedBy INTEGER, " +
                    "processedDate DATETIME, " +
                    "remarks TEXT, " +
                    "accountNumber INTEGER, " +
                    "FOREIGN KEY (processedBy) REFERENCES staff(staffId))";
            stmt.execute(accountRequestsTable);
            
            // Create staff_activity_log table for audit trail
            String staffActivityTable = "CREATE TABLE IF NOT EXISTS staff_activity_log (" +
                    "logId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "staffId INTEGER NOT NULL, " +
                    "action TEXT NOT NULL, " +
                    "targetAccount INTEGER, " +
                    "details TEXT, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "ipAddress TEXT, " +
                    "FOREIGN KEY (staffId) REFERENCES staff(staffId))";
            stmt.execute(staffActivityTable);
            
            // Initialize Admin Module tables
            initializeAdminTables();
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }
    
    /**
     * Initialize Admin Module database tables
     */
    private void initializeAdminTables() {
        try {
            com.mybank.database.AdminDAO.createTable();
            com.mybank.database.AuditLogDAO.createTable();
            com.mybank.database.BankConfigDAO.createTable();
            System.out.println("Admin module tables initialized successfully!");
        } catch (Exception e) {
            System.err.println("Error initializing admin tables: " + e.getMessage());
        }
    }
    
    /**
     * Hashes a password using SHA-256
     * @param password The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Authenticates a user login
     * @param accountNumber The account number
     * @param password The plain text password
     * @return true if authentication successful, false otherwise
     */
    public boolean authenticateLogin(int accountNumber, String password) {
        Connection conn = getConnection();
        if (conn == null) return false;
        
        String sql = "SELECT passwordHash, status, failedAttempts FROM accounts WHERE accountNumber = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("passwordHash");
                String status = rs.getString("status");
                
                // Check if account is blocked
                if ("BLOCKED".equals(status)) {
                    return false;
                }
                
                // Check if password hash is empty (old account without password)
                if (storedHash == null || storedHash.isEmpty()) {
                    return false;
                }
                
                // Verify password
                String inputHash = hashPassword(password);
                if (storedHash.equals(inputHash)) {
                    // Reset failed attempts on successful login
                    resetFailedAttempts(accountNumber);
                    return true;
                } else {
                    // Increment failed attempts
                    incrementFailedAttempts(accountNumber);
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating login: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Increments failed login attempts and blocks account if necessary
     * @param accountNumber The account number
     */
    private void incrementFailedAttempts(int accountNumber) {
        Connection conn = getConnection();
        if (conn == null) return;
        
        String sql = "UPDATE accounts SET failedAttempts = failedAttempts + 1 WHERE accountNumber = ?";
        String blockSql = "UPDATE accounts SET status = 'BLOCKED' WHERE accountNumber = ? AND failedAttempts >= 3";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             PreparedStatement blockStmt = conn.prepareStatement(blockSql)) {
            
            pstmt.setInt(1, accountNumber);
            pstmt.executeUpdate();
            
            blockStmt.setInt(1, accountNumber);
            blockStmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error incrementing failed attempts: " + e.getMessage());
        }
    }
    
    /**
     * Resets failed login attempts on successful login
     * @param accountNumber The account number
     */
    private void resetFailedAttempts(int accountNumber) {
        Connection conn = getConnection();
        if (conn == null) return;
        
        String sql = "UPDATE accounts SET failedAttempts = 0 WHERE accountNumber = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error resetting failed attempts: " + e.getMessage());
        }
    }
    
    /**
     * Gets the account status
     * @param accountNumber The account number
     * @return Account status (ACTIVE/BLOCKED) or null if not found
     */
    public String getAccountStatus(int accountNumber) {
        Connection conn = getConnection();
        if (conn == null) return null;
        
        String sql = "SELECT status FROM accounts WHERE accountNumber = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            System.err.println("Error getting account status: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Gets failed login attempts count
     * @param accountNumber The account number
     * @return Number of failed attempts
     */
    public int getFailedAttempts(int accountNumber) {
        Connection conn = getConnection();
        if (conn == null) return 0;
        
        String sql = "SELECT failedAttempts FROM accounts WHERE accountNumber = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("failedAttempts");
            }
        } catch (SQLException e) {
            System.err.println("Error getting failed attempts: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Authenticates a user by full name and phone number (for first-time login)
     * @param fullName The customer's full name
     * @param phoneNumber The customer's phone number
     * @return Account number if found, -1 otherwise
     */
    public int authenticateByNameAndPhone(String fullName, String phoneNumber) {
        // Ensure connection is valid
        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("ERROR: Unable to get database connection");
            return -1;
        }
        
        String sql = "SELECT accountNumber, ownerName, phoneNumber FROM accounts WHERE LOWER(TRIM(ownerName)) = LOWER(?) AND phoneNumber = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String trimmedName = fullName.trim();
            String trimmedPhone = phoneNumber.trim();
            
            pstmt.setString(1, trimmedName);
            pstmt.setString(2, trimmedPhone);
            
            System.out.println("=== DEBUG: Authenticating by Name and Phone ===");
            System.out.println("Name: '" + trimmedName + "' (length: " + trimmedName.length() + ")");
            System.out.println("Phone: '" + trimmedPhone + "' (length: " + trimmedPhone.length() + ")");
            System.out.println("SQL: " + sql);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int accountNum = rs.getInt("accountNumber");
                String dbName = rs.getString("ownerName");
                String dbPhone = rs.getString("phoneNumber");
                System.out.println("SUCCESS: Account found!");
                System.out.println("  Account #: " + accountNum);
                System.out.println("  DB Name: '" + dbName + "'");
                System.out.println("  DB Phone: '" + dbPhone + "'");
                return accountNum;
            } else {
                System.out.println("FAILED: No account found matching the criteria");
                // Try to find similar accounts for debugging
                String debugSql = "SELECT accountNumber, ownerName, phoneNumber FROM accounts WHERE phoneNumber = ?";
                try (PreparedStatement debugStmt = conn.prepareStatement(debugSql)) {
                    debugStmt.setString(1, trimmedPhone);
                    ResultSet debugRs = debugStmt.executeQuery();
                    if (debugRs.next()) {
                        System.out.println("  Found account with same phone but different name:");
                        System.out.println("    DB Name: '" + debugRs.getString("ownerName") + "'");
                        System.out.println("    Account #: " + debugRs.getInt("accountNumber"));
                    }
                } catch (SQLException ex) {
                    // Ignore debug errors
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR authenticating by name and phone: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Gets account number by phone number
     * @param phoneNumber The phone number
     * @return Account number if found, -1 otherwise
     */
    public int getAccountByPhone(String phoneNumber) {
        Connection conn = getConnection();
        if (conn == null) return -1;
        
        String sql = "SELECT accountNumber FROM accounts WHERE phoneNumber = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNumber.trim());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("accountNumber");
            }
        } catch (SQLException e) {
            System.err.println("Error getting account by phone: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Checks if an account has a password set
     * @param accountNumber The account number
     * @return true if password is set, false otherwise
     */
    public boolean hasPassword(int accountNumber) {
        Connection conn = getConnection();
        if (conn == null) return false;
        
        String sql = "SELECT passwordHash FROM accounts WHERE accountNumber = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String passwordHash = rs.getString("passwordHash");
                return passwordHash != null && !passwordHash.isEmpty();
            }
        } catch (SQLException e) {
            System.err.println("Error checking password: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Checks if account is still using the default password
     * @param accountNumber The account number
     * @return true if using default password, false otherwise
     */
    public boolean isUsingDefaultPassword(int accountNumber) {
        Connection conn = getConnection();
        if (conn == null) return false;
        
        String sql = "SELECT passwordHash FROM accounts WHERE accountNumber = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String passwordHash = rs.getString("passwordHash");
                String defaultHash = hashPassword(DEFAULT_PASSWORD);
                return passwordHash != null && passwordHash.equals(defaultHash);
            }
        } catch (SQLException e) {
            System.err.println("Error checking default password: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Updates/sets the password for an account
     * @param accountNumber The account number
     * @param newPassword The new plain text password
     * @return true if successful, false otherwise
     */
    public boolean updatePassword(int accountNumber, String newPassword) {
        String sql = "UPDATE accounts SET passwordHash = ? WHERE accountNumber = ?";
        ensureConnection();
        
        try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
            pstmt.setString(1, hashPassword(newPassword));
            pstmt.setInt(2, accountNumber);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the account owner name (alias for getOwnerName)
     * @param accountNumber The account number
     * @return The owner name, or null if not found
     */
    public String getAccountOwnerName(int accountNumber) {
        return getOwnerName(accountNumber);
    }
    
    /**
     * Creates a new bank account with password
     * @param accountNumber The account number
     * @param ownerName The account owner's name
     * @param password The plain text password
     * @param initialDeposit The initial deposit amount
     * @return true if successful, false otherwise
     */
    public boolean createAccount(int accountNumber, String ownerName, String password, double initialDeposit) {
        synchronized (connectionLock) {
            ensureConnection();
            String sql = "INSERT INTO accounts (accountNumber, ownerName, passwordHash, balance, status, failedAttempts) VALUES (?, ?, ?, ?, 'ACTIVE', 0)";
            
            try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
                pstmt.setInt(1, accountNumber);
                pstmt.setString(2, ownerName);
                pstmt.setString(3, hashPassword(password));
                pstmt.setDouble(4, initialDeposit);
                pstmt.executeUpdate();
                
                // Record initial deposit as a transaction if > 0
                if (initialDeposit > 0) {
                    recordTransaction(accountNumber, "Initial Deposit", initialDeposit);
                }
                
                return true;
            } catch (SQLException e) {
                System.err.println("Error creating account: " + e.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Creates a new bank account (legacy method without password - for backward compatibility)
     * @param accountNumber The account number
     * @param ownerName The account owner's name
     * @param initialDeposit The initial deposit amount
     * @return true if successful, false otherwise
     */
    public boolean createAccount(int accountNumber, String ownerName, double initialDeposit) {
        String sql = "INSERT INTO accounts (accountNumber, ownerName, passwordHash, balance, status, failedAttempts) VALUES (?, ?, '', ?, 'ACTIVE', 0)";
        ensureConnection();
        
        try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            pstmt.setString(2, ownerName);
            pstmt.setDouble(3, initialDeposit);
            pstmt.executeUpdate();
            
            // Record initial deposit as a transaction if > 0
            if (initialDeposit > 0) {
                recordTransaction(accountNumber, "Initial Deposit", initialDeposit);
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates a new bank account with full details (for staff approval)
     * Auto-generates account number
     * @param ownerName The account owner's name
     * @param password The account password
     * @param initialDeposit The initial deposit amount
     * @param email Customer email
     * @param phoneNumber Customer phone number
     * @param smsEnabled Whether SMS notifications are enabled
     * @param emailEnabled Whether email notifications are enabled
     * @param accountType Account type (SAVINGS, CURRENT, LOAN)
     * @return true if successful, false otherwise
     */
    public static boolean createAccount(String ownerName, String password, double initialDeposit,
                                       String email, String phoneNumber, boolean smsEnabled,
                                       boolean emailEnabled, String accountType) {
        synchronized (connectionLock) {
            try {
                ensureConnection();
                // Generate new account number
                int newAccountNumber = generateAccountNumber();
                
                String sql = "INSERT INTO accounts (accountNumber, ownerName, passwordHash, balance, " +
                           "status, failedAttempts, email, phoneNumber, smsEnabled, emailEnabled, " +
                           "accountType, availableBalance, createdDate) " +
                           "VALUES (?, ?, ?, ?, 'ACTIVE', 0, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                
                try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
                    pstmt.setInt(1, newAccountNumber);
                    pstmt.setString(2, ownerName);
                    pstmt.setString(3, hashPassword(password));
                    pstmt.setDouble(4, initialDeposit);
                    pstmt.setString(5, email);
                    pstmt.setString(6, phoneNumber);
                    pstmt.setInt(7, smsEnabled ? 1 : 0);
                    pstmt.setInt(8, emailEnabled ? 1 : 0);
                    pstmt.setString(9, accountType);
                    pstmt.setDouble(10, initialDeposit);
                    
                    pstmt.executeUpdate();
                    
                    // Record initial deposit as a transaction if > 0
                    if (initialDeposit > 0) {
                        recordTransaction(newAccountNumber, "Initial Deposit", initialDeposit);
                    }
                    
                    System.out.println("Account created successfully. Account Number: " + newAccountNumber);
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("Error creating account: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }
    
    /**
     * Create a new bank account using provided connection (for transactions)
     */
    public static int createAccountWithConnection(Connection conn, String ownerName, String password, 
                                                  double initialDeposit, String email, String phoneNumber, 
                                                  boolean smsEnabled, boolean emailEnabled, String accountType,
                                                  String profilePicturePath) throws SQLException {
        // Generate new account number
        int newAccountNumber = generateAccountNumber();
        
        String sql = "INSERT INTO accounts (accountNumber, ownerName, passwordHash, balance, " +
                   "status, failedAttempts, email, phoneNumber, smsEnabled, emailEnabled, " +
                   "accountType, availableBalance, profilePicturePath, createdDate) " +
                   "VALUES (?, ?, ?, ?, 'ACTIVE', 0, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newAccountNumber);
            pstmt.setString(2, ownerName);
            pstmt.setString(3, hashPassword(password));
            pstmt.setDouble(4, initialDeposit);
            pstmt.setString(5, email);
            pstmt.setString(6, phoneNumber);
            pstmt.setInt(7, smsEnabled ? 1 : 0);
            pstmt.setInt(8, emailEnabled ? 1 : 0);
            pstmt.setString(9, accountType);
            pstmt.setDouble(10, initialDeposit);
            pstmt.setString(11, profilePicturePath);
            
            pstmt.executeUpdate();
            
            // Record initial deposit as a transaction if > 0
            if (initialDeposit > 0) {
                recordTransactionWithConnection(conn, newAccountNumber, "Initial Deposit", initialDeposit);
            }
            
            System.out.println("Account created successfully. Account Number: " + newAccountNumber);
            return newAccountNumber;
        }
    }
    
    /**
     * Record transaction using provided connection (for transactions)
     */
    private static void recordTransactionWithConnection(Connection conn, int accountNumber, String type, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (accountNumber, type, amount, timestamp) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            
            // Get current date and time
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            pstmt.setString(4, now.format(formatter));
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Generate a new unique account number
     * @return New account number
     */
    private static int generateAccountNumber() {
        synchronized (connectionLock) {
            try {
                ensureConnection();
                String query = "SELECT MAX(accountNumber) FROM accounts";
                try (Statement stmt = sharedConnection.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    if (rs.next()) {
                        int maxAccount = rs.getInt(1);
                        return maxAccount + 1;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error generating account number: " + e.getMessage());
            }
            // Default starting account number
            return 10000;
        }
    }
    
    /**
     * Checks if an account exists
     * @param accountNumber The account number to check
     * @return true if exists, false otherwise
     */
    public boolean accountExists(int accountNumber) {
        String sql = "SELECT accountNumber FROM accounts WHERE accountNumber = ?";
        ensureConnection();
        
        try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking account: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the current balance of an account
     * @param accountNumber The account number
     * @return The balance, or -1 if account doesn't exist
     */
    public double getBalance(int accountNumber) {
        String sql = "SELECT balance FROM accounts WHERE accountNumber = ?";
        ensureConnection();
        
        try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.err.println("Error getting balance: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Gets the owner name of an account
     * @param accountNumber The account number
     * @return The owner name, or null if not found
     */
    public String getOwnerName(int accountNumber) {
        String sql = "SELECT ownerName FROM accounts WHERE accountNumber = ?";
        ensureConnection();
        
        try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("ownerName");
            }
        } catch (SQLException e) {
            System.err.println("Error getting owner name: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Deposits money into an account
     * @param accountNumber The account number
     * @param amount The amount to deposit
     * @return true if successful, false otherwise
     */
    public boolean deposit(int accountNumber, double amount) {
        synchronized (connectionLock) {
            if (!accountExists(accountNumber) || amount <= 0) {
                return false;
            }
            
            ensureConnection();
            String sql = "UPDATE accounts SET balance = balance + ? WHERE accountNumber = ?";
            
            try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, accountNumber);
                pstmt.executeUpdate();
                
                // Record transaction
                recordTransaction(accountNumber, "Deposit", amount);
                
                return true;
            } catch (SQLException e) {
                System.err.println("Error depositing money: " + e.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Withdraws money from an account
     * @param accountNumber The account number
     * @param amount The amount to withdraw
     * @return true if successful, false otherwise
     */
    public boolean withdraw(int accountNumber, double amount) {
        synchronized (connectionLock) {
            if (!accountExists(accountNumber) || amount <= 0) {
                return false;
            }
            
            double currentBalance = getBalance(accountNumber);
            if (currentBalance < amount) {
                return false; // Insufficient balance
            }
            
            ensureConnection();
            String sql = "UPDATE accounts SET balance = balance - ? WHERE accountNumber = ?";
            
            try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, accountNumber);
                pstmt.executeUpdate();
                
                // Record transaction
                recordTransaction(accountNumber, "Withdraw", amount);
                
                return true;
            } catch (SQLException e) {
                System.err.println("Error withdrawing money: " + e.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Transfers money between two accounts
     * @param fromAccount The sender's account number
     * @param toAccount The receiver's account number
     * @param amount The amount to transfer
     * @return true if successful, false otherwise
     */
    public boolean transfer(int fromAccount, int toAccount, double amount) {
        synchronized (connectionLock) {
            if (!accountExists(fromAccount) || !accountExists(toAccount) || amount <= 0) {
                return false;
            }
            
            if (fromAccount == toAccount) {
                return false; // Cannot transfer to same account
            }
            
            double senderBalance = getBalance(fromAccount);
            if (senderBalance < amount) {
                return false; // Insufficient balance
            }
            
            ensureConnection();
            try {
                // Start transaction
                sharedConnection.setAutoCommit(false);
            
            // Deduct from sender
            String deductSql = "UPDATE accounts SET balance = balance - ? WHERE accountNumber = ?";
            try (PreparedStatement pstmt = sharedConnection.prepareStatement(deductSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, fromAccount);
                pstmt.executeUpdate();
            }
            
            // Add to receiver
            String addSql = "UPDATE accounts SET balance = balance + ? WHERE accountNumber = ?";
            try (PreparedStatement pstmt = sharedConnection.prepareStatement(addSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, toAccount);
                pstmt.executeUpdate();
            }
            
            // Record transactions
            recordTransaction(fromAccount, "Transfer Out to " + toAccount, amount);
            recordTransaction(toAccount, "Transfer In from " + fromAccount, amount);
            
            // Commit transaction
            sharedConnection.commit();
            sharedConnection.setAutoCommit(true);
            
            return true;
        } catch (SQLException e) {
            try {
                sharedConnection.rollback();
                sharedConnection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Rollback error: " + ex.getMessage());
            }
            System.err.println("Error transferring money: " + e.getMessage());
            return false;
            }
        }
    }
    
    /**
     * Records a transaction in the database
     * @param accountNumber The account number
     * @param type The transaction type
     * @param amount The transaction amount
     */
    private static void recordTransaction(int accountNumber, String type, double amount) {
        synchronized (connectionLock) {
            ensureConnection();
            String sql = "INSERT INTO transactions (accountNumber, type, amount, timestamp) VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
                pstmt.setInt(1, accountNumber);
                pstmt.setString(2, type);
                pstmt.setDouble(3, amount);
                
                // Get current date and time
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                pstmt.setString(4, now.format(formatter));
                
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error recording transaction: " + e.getMessage());
            }
        }
    }
    
    /**
     * Gets transaction history for an account
     * @param accountNumber The account number
     * @return ResultSet containing transaction history
     */
    public ResultSet getTransactionHistory(int accountNumber) {
        String sql = "SELECT * FROM transactions WHERE accountNumber = ? ORDER BY id DESC";
        
        try {
            ensureConnection();
            PreparedStatement pstmt = sharedConnection.prepareStatement(sql);
            pstmt.setInt(1, accountNumber);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error getting transaction history: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Closes the database connection
     */
    public void closeConnection() {
        try {
            if (sharedConnection != null && !sharedConnection.isClosed()) {
                sharedConnection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Updates account contact information
     * @param accountNumber The account number
     * @param email The email address
     * @param phoneNumber The phone number
     * @return true if successful, false otherwise
     */
    public boolean updateContactInfo(int accountNumber, String email, String phoneNumber) {
        synchronized (connectionLock) {
            ensureConnection();
            String sql = "UPDATE accounts SET email = ?, phoneNumber = ? WHERE accountNumber = ?";
            
            try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
                pstmt.setString(1, email);
                pstmt.setString(2, phoneNumber);
                pstmt.setInt(3, accountNumber);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("Error updating contact info: " + e.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Updates notification preferences
     * @param accountNumber The account number
     * @param smsEnabled SMS notifications enabled
     * @param emailEnabled Email notifications enabled
     * @return true if successful, false otherwise
     */
    public boolean updateNotificationPreferences(int accountNumber, boolean smsEnabled, boolean emailEnabled) {
        synchronized (connectionLock) {
            ensureConnection();
            String sql = "UPDATE accounts SET smsEnabled = ?, emailEnabled = ? WHERE accountNumber = ?";
            
            try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
                pstmt.setInt(1, smsEnabled ? 1 : 0);
                pstmt.setInt(2, emailEnabled ? 1 : 0);
                pstmt.setInt(3, accountNumber);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("Error updating notification preferences: " + e.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Gets account contact information and preferences
     * @param accountNumber The account number
     * @return Array: [email, phoneNumber, smsEnabled, emailEnabled]
     */
    public String[] getAccountContactInfo(int accountNumber) {
        String sql = "SELECT email, phoneNumber, smsEnabled, emailEnabled FROM accounts WHERE accountNumber = ?";
        ensureConnection();
        
        try (PreparedStatement pstmt = sharedConnection.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new String[] {
                    rs.getString("email"),
                    rs.getString("phoneNumber"),
                    String.valueOf(rs.getInt("smsEnabled")),
                    String.valueOf(rs.getInt("emailEnabled"))
                };
            }
        } catch (SQLException e) {
            System.err.println("Error getting contact info: " + e.getMessage());
        }
        return null;
    }
}

