-- =====================================================
-- First Login Password Change Migration
-- =====================================================

-- Add is_first_login column to Users table
ALTER TABLE Users 
ADD COLUMN IF NOT EXISTS is_first_login BOOLEAN DEFAULT TRUE;

-- Update existing users to set is_first_login to false (assuming they've already logged in)
-- You may want to adjust this based on your requirements
UPDATE Users 
SET is_first_login = FALSE 
WHERE is_first_login IS NULL OR is_first_login = TRUE;

-- For new users, is_first_login will default to TRUE
-- This ensures that newly created users will be prompted to change their password on first login

