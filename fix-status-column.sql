-- Fix the status column length to support enum values
-- Run this script in your MySQL database

USE classroom_support_system;

-- Alter the chat_sessions table to increase status column length
ALTER TABLE chat_sessions MODIFY COLUMN status VARCHAR(20) NOT NULL;

-- Verify the change
DESCRIBE chat_sessions;

-- Check existing data
SELECT id, status, started_at FROM chat_sessions;
