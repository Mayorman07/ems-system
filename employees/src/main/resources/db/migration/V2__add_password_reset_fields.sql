
ALTER TABLE employees
ADD COLUMN password_reset_token VARCHAR(255) NULL,
ADD COLUMN password_reset_token_expiry_date DATETIME(6) NULL;