-- ========================================
-- DELETE TEST USERS FROM DATABASE
-- ========================================
-- Use this script to clean up test accounts
-- Run this in MySQL Workbench or Aiven Console

-- 1. VIEW ALL USERS FIRST (to see what you're deleting)
SELECT id, username, email, role, verified, created_at 
FROM users 
ORDER BY created_at DESC;

-- 2. DELETE SPECIFIC USER BY EMAIL
-- Uncomment and modify as needed:
-- DELETE FROM users WHERE email = 'test@example.com';

-- 3. DELETE SPECIFIC USER BY USERNAME
-- DELETE FROM users WHERE username = 'testuser';

-- 4. DELETE ALL UNVERIFIED USERS
-- DELETE FROM users WHERE verified = 0;

-- 5. DELETE ALL USERS (USE WITH CAUTION!)
-- Option A: If safe mode is disabled (after reconnecting)
-- DELETE FROM users;

-- Option B: If safe mode is still enabled, delete by ID range
-- First, check the max ID:
-- SELECT MAX(id) FROM users;
-- Then delete all users with ID <= max_id (replace 1000 with your max ID)
DELETE FROM users WHERE id > 0 AND id <= 1000;

-- 6. RESET AUTO-INCREMENT ID (optional, after deleting all)
ALTER TABLE users AUTO_INCREMENT = 1;

-- 7. VERIFY DELETION
SELECT COUNT(*) as total_users FROM users;
