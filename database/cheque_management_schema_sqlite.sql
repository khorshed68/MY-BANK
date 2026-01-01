-- ============================================
-- CHEQUE MANAGEMENT SYSTEM - SQLite Schema
-- Compatible with existing MY BANK SQLite database
-- ============================================

-- Drop existing tables if they exist
DROP TABLE IF EXISTS cheque_transactions;
DROP TABLE IF EXISTS cheques;
DROP TABLE IF EXISTS cheque_books;
DROP TABLE IF EXISTS cheque_book_eligibility;

-- ============================================
-- 1. CHEQUE BOOK ELIGIBILITY TABLE
-- Defines criteria for cheque book eligibility by account type
-- ============================================
CREATE TABLE cheque_book_eligibility (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_type TEXT NOT NULL UNIQUE,
    minimum_balance REAL NOT NULL DEFAULT 5000.00,
    minimum_account_age_days INTEGER NOT NULL DEFAULT 90,
    max_books_per_year INTEGER NOT NULL DEFAULT 2,
    leaves_per_book INTEGER NOT NULL DEFAULT 25,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 2. CHEQUE BOOKS TABLE
-- Stores cheque book requests and approvals
-- ============================================
CREATE TABLE cheque_books (
    cheque_book_id INTEGER PRIMARY KEY AUTOINCREMENT,
    accountNumber INTEGER NOT NULL,
    book_number TEXT NOT NULL UNIQUE,
    start_cheque_number TEXT NOT NULL,
    end_cheque_number TEXT NOT NULL,
    total_leaves INTEGER NOT NULL DEFAULT 25,
    remaining_leaves INTEGER NOT NULL DEFAULT 25,
    status TEXT NOT NULL DEFAULT 'PENDING',
    request_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    approved_by INTEGER,
    approval_date DATETIME,
    remarks TEXT,
    FOREIGN KEY (accountNumber) REFERENCES accounts(accountNumber) ON DELETE CASCADE,
    CHECK (status IN ('PENDING', 'APPROVED', 'ISSUED', 'REJECTED', 'CANCELLED', 'COMPLETED'))
);

-- ============================================
-- 3. CHEQUES TABLE
-- Stores individual cheque details
-- ============================================
CREATE TABLE cheques (
    cheque_id INTEGER PRIMARY KEY AUTOINCREMENT,
    cheque_book_id INTEGER NOT NULL,
    accountNumber INTEGER NOT NULL,
    cheque_number TEXT NOT NULL UNIQUE,
    amount REAL DEFAULT 0.00,
    payee_name TEXT,
    issue_date DATE,
    deposit_date DATETIME,
    clearance_date DATETIME,
    status TEXT NOT NULL DEFAULT 'ISSUED',
    signature_verified INTEGER DEFAULT 0,
    bounce_reason TEXT,
    processed_by INTEGER,
    deposited_to_account INTEGER,
    remarks TEXT,
    FOREIGN KEY (cheque_book_id) REFERENCES cheque_books(cheque_book_id) ON DELETE CASCADE,
    FOREIGN KEY (accountNumber) REFERENCES accounts(accountNumber) ON DELETE CASCADE,
    CHECK (status IN ('ISSUED', 'DEPOSITED', 'CLEARED', 'BOUNCED', 'CANCELLED', 'VOID'))
);

-- ============================================
-- 4. CHEQUE TRANSACTIONS TABLE
-- Audit trail for all cheque-related operations
-- ============================================
CREATE TABLE cheque_transactions (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    cheque_id INTEGER NOT NULL,
    cheque_number TEXT NOT NULL,
    accountNumber INTEGER NOT NULL,
    transaction_type TEXT NOT NULL,
    old_status TEXT,
    new_status TEXT NOT NULL,
    amount REAL DEFAULT 0.00,
    performed_by INTEGER NOT NULL,
    user_type TEXT NOT NULL,
    performed_by_name TEXT,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    remarks TEXT,
    bounce_reason TEXT,
    FOREIGN KEY (cheque_id) REFERENCES cheques(cheque_id) ON DELETE CASCADE,
    FOREIGN KEY (accountNumber) REFERENCES accounts(accountNumber) ON DELETE CASCADE,
    CHECK (transaction_type IN ('ISSUE', 'DEPOSIT', 'CLEAR', 'BOUNCE', 'CANCEL', 'VOID')),
    CHECK (user_type IN ('CUSTOMER', 'STAFF', 'ADMIN'))
);

-- ============================================
-- INDEXES for Performance
-- ============================================
CREATE INDEX idx_cheque_books_account ON cheque_books(accountNumber);
CREATE INDEX idx_cheque_books_status ON cheque_books(status);
CREATE INDEX idx_cheque_books_request_date ON cheque_books(request_date);

CREATE INDEX idx_cheques_book ON cheques(cheque_book_id);
CREATE INDEX idx_cheques_account ON cheques(accountNumber);
CREATE INDEX idx_cheques_number ON cheques(cheque_number);
CREATE INDEX idx_cheques_status ON cheques(status);

CREATE INDEX idx_transactions_cheque ON cheque_transactions(cheque_id);
CREATE INDEX idx_transactions_account ON cheque_transactions(accountNumber);
CREATE INDEX idx_transactions_date ON cheque_transactions(transaction_date);

-- ============================================
-- DEFAULT ELIGIBILITY DATA
-- ============================================
INSERT INTO cheque_book_eligibility (account_type, minimum_balance, minimum_account_age_days, max_books_per_year, leaves_per_book, is_active)
VALUES 
    ('SAVINGS', 10000.00, 180, 2, 25, 1),
    ('CURRENT', 5000.00, 90, 4, 50, 1),
    ('SALARY', 5000.00, 30, 3, 25, 1);

-- ============================================
-- VIEWS for Easy Querying
-- ============================================

-- View: Cheque Book Summary
CREATE VIEW vw_cheque_book_summary AS
SELECT 
    cb.cheque_book_id,
    cb.accountNumber,
    a.ownerName as customer_name,
    cb.book_number,
    cb.start_cheque_number,
    cb.end_cheque_number,
    cb.total_leaves,
    cb.remaining_leaves,
    cb.status,
    cb.request_date,
    cb.approval_date,
    cb.approved_by,
    s.fullName as approved_by_name,
    a.balance as current_balance,
    cb.remarks
FROM cheque_books cb
JOIN accounts a ON cb.accountNumber = a.accountNumber
LEFT JOIN staff s ON cb.approved_by = s.staffId;

-- View: Cheque Details
CREATE VIEW vw_cheque_details AS
SELECT 
    c.cheque_id,
    c.cheque_number,
    c.cheque_book_id,
    cb.book_number,
    c.accountNumber,
    a.ownerName as account_holder,
    c.amount,
    c.payee_name,
    c.issue_date,
    c.deposit_date,
    c.clearance_date,
    c.status,
    c.signature_verified,
    c.bounce_reason,
    c.deposited_to_account,
    c.processed_by,
    s.fullName as processed_by_name,
    c.remarks
FROM cheques c
JOIN cheque_books cb ON c.cheque_book_id = cb.cheque_book_id
JOIN accounts a ON c.accountNumber = a.accountNumber
LEFT JOIN staff s ON c.processed_by = s.staffId;

-- View: Transaction History
CREATE VIEW vw_cheque_transaction_history AS
SELECT 
    ct.transaction_id,
    ct.cheque_id,
    ct.cheque_number,
    ct.accountNumber,
    a.ownerName as account_holder,
    ct.transaction_type,
    ct.old_status,
    ct.new_status,
    ct.amount,
    ct.performed_by,
    ct.user_type,
    ct.performed_by_name,
    ct.transaction_date,
    ct.remarks,
    ct.bounce_reason
FROM cheque_transactions ct
JOIN accounts a ON ct.accountNumber = a.accountNumber
ORDER BY ct.transaction_date DESC;

-- ============================================
-- Schema creation complete
-- ============================================
