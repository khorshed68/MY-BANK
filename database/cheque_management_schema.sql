-- Cheque Management System Database Schema
-- Created: 2025-12-31

-- Table to store cheque book information
CREATE TABLE IF NOT EXISTS cheque_books (
    cheque_book_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    customer_id INT NOT NULL,
    book_number VARCHAR(20) UNIQUE NOT NULL,
    start_cheque_number VARCHAR(15) NOT NULL,
    end_cheque_number VARCHAR(15) NOT NULL,
    total_leaves INT NOT NULL DEFAULT 25,
    remaining_leaves INT NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approval_date TIMESTAMP NULL,
    approved_by INT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'ISSUED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    rejection_reason VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_account (account_id),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_book_number (book_number)
);

-- Table to store individual cheque information
CREATE TABLE IF NOT EXISTS cheques (
    cheque_id INT PRIMARY KEY AUTO_INCREMENT,
    cheque_book_id INT NOT NULL,
    account_id INT NOT NULL,
    customer_id INT NOT NULL,
    cheque_number VARCHAR(15) UNIQUE NOT NULL,
    amount DECIMAL(15, 2) NULL,
    payee_name VARCHAR(200) NULL,
    issue_date DATE NULL,
    deposit_date TIMESTAMP NULL,
    deposited_by_account INT NULL,
    deposited_by_customer INT NULL,
    clearance_date TIMESTAMP NULL,
    status ENUM('ISSUED', 'DEPOSITED', 'PENDING_CLEARANCE', 'CLEARED', 'BOUNCED', 'CANCELLED', 'VOID') DEFAULT 'ISSUED',
    bounce_reason VARCHAR(500) NULL,
    processed_by INT NULL,
    signature_verified BOOLEAN DEFAULT FALSE,
    remarks VARCHAR(1000) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cheque_book_id) REFERENCES cheque_books(cheque_book_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (deposited_by_account) REFERENCES accounts(account_id) ON DELETE SET NULL,
    FOREIGN KEY (deposited_by_customer) REFERENCES customers(customer_id) ON DELETE SET NULL,
    INDEX idx_cheque_number (cheque_number),
    INDEX idx_account (account_id),
    INDEX idx_status (status),
    INDEX idx_cheque_book (cheque_book_id)
);

-- Table to store cheque transaction history
CREATE TABLE IF NOT EXISTS cheque_transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    cheque_id INT NOT NULL,
    cheque_number VARCHAR(15) NOT NULL,
    account_id INT NOT NULL,
    transaction_type ENUM('ISSUE', 'DEPOSIT', 'CLEAR', 'BOUNCE', 'CANCEL', 'VOID') NOT NULL,
    old_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NULL,
    performed_by INT NOT NULL,
    user_type ENUM('CUSTOMER', 'STAFF', 'ADMIN') NOT NULL,
    remarks VARCHAR(1000) NULL,
    bounce_reason VARCHAR(500) NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cheque_id) REFERENCES cheques(cheque_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    INDEX idx_cheque (cheque_id),
    INDEX idx_account (account_id),
    INDEX idx_type (transaction_type),
    INDEX idx_date (transaction_date)
);

-- Table to track cheque book eligibility criteria
CREATE TABLE IF NOT EXISTS cheque_book_eligibility (
    id INT PRIMARY KEY AUTO_INCREMENT,
    account_type VARCHAR(50) NOT NULL,
    minimum_balance DECIMAL(15, 2) NOT NULL DEFAULT 5000.00,
    minimum_account_age_days INT NOT NULL DEFAULT 90,
    max_books_per_year INT NOT NULL DEFAULT 4,
    leaves_per_book INT NOT NULL DEFAULT 25,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY (account_type)
);

-- Insert default eligibility criteria
INSERT INTO cheque_book_eligibility (account_type, minimum_balance, minimum_account_age_days, max_books_per_year, leaves_per_book) VALUES
('SAVINGS', 5000.00, 90, 2, 25),
('CURRENT', 10000.00, 30, 6, 50),
('SALARY', 3000.00, 30, 2, 25) 
ON DUPLICATE KEY UPDATE account_type=account_type;

-- View for cheque book summary
CREATE OR REPLACE VIEW vw_cheque_book_summary AS
SELECT 
    cb.cheque_book_id,
    cb.book_number,
    cb.account_id,
    a.account_number,
    cb.customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    cb.start_cheque_number,
    cb.end_cheque_number,
    cb.total_leaves,
    cb.remaining_leaves,
    cb.status AS book_status,
    cb.request_date,
    cb.approval_date,
    cb.approved_by,
    CASE 
        WHEN cb.approved_by IS NOT NULL THEN 
            COALESCE(CONCAT(s.first_name, ' ', s.last_name), CONCAT(ad.username, ' (Admin)'))
        ELSE NULL
    END AS approved_by_name,
    a.account_type,
    a.balance AS current_balance
FROM cheque_books cb
JOIN accounts a ON cb.account_id = a.account_id
JOIN customers c ON cb.customer_id = c.customer_id
LEFT JOIN staff s ON cb.approved_by = s.staff_id
LEFT JOIN admins ad ON cb.approved_by = ad.admin_id;

-- View for cheque details
CREATE OR REPLACE VIEW vw_cheque_details AS
SELECT 
    ch.cheque_id,
    ch.cheque_number,
    ch.account_id,
    a.account_number,
    ch.customer_id,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    ch.amount,
    ch.payee_name,
    ch.issue_date,
    ch.deposit_date,
    ch.clearance_date,
    ch.status,
    ch.bounce_reason,
    ch.signature_verified,
    ch.remarks,
    cb.book_number,
    a.balance AS current_balance,
    CASE 
        WHEN ch.deposited_by_customer IS NOT NULL THEN 
            CONCAT(dc.first_name, ' ', dc.last_name)
        ELSE NULL
    END AS deposited_by_name,
    da.account_number AS deposited_to_account
FROM cheques ch
JOIN accounts a ON ch.account_id = a.account_id
JOIN customers c ON ch.customer_id = c.customer_id
JOIN cheque_books cb ON ch.cheque_book_id = cb.cheque_book_id
LEFT JOIN customers dc ON ch.deposited_by_customer = dc.customer_id
LEFT JOIN accounts da ON ch.deposited_by_account = da.account_id;

-- View for cheque transaction history
CREATE OR REPLACE VIEW vw_cheque_transaction_history AS
SELECT 
    ct.transaction_id,
    ct.cheque_number,
    ct.transaction_type,
    ct.old_status,
    ct.new_status,
    ct.amount,
    ct.transaction_date,
    ct.user_type,
    ct.remarks,
    ct.bounce_reason,
    a.account_number,
    CONCAT(c.first_name, ' ', c.last_name) AS account_holder,
    CASE 
        WHEN ct.user_type = 'CUSTOMER' THEN CONCAT(cust.first_name, ' ', cust.last_name)
        WHEN ct.user_type = 'STAFF' THEN CONCAT(s.first_name, ' ', s.last_name)
        WHEN ct.user_type = 'ADMIN' THEN ad.username
    END AS performed_by_name
FROM cheque_transactions ct
JOIN accounts a ON ct.account_id = a.account_id
JOIN customers c ON a.customer_id = c.customer_id
LEFT JOIN customers cust ON ct.performed_by = cust.customer_id AND ct.user_type = 'CUSTOMER'
LEFT JOIN staff s ON ct.performed_by = s.staff_id AND ct.user_type = 'STAFF'
LEFT JOIN admins ad ON ct.performed_by = ad.admin_id AND ct.user_type = 'ADMIN';
