-- ============================================
-- FIX CHEQUE MANAGEMENT VIEWS - SQLite Compatible
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
    a.accountType as account_type,
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
