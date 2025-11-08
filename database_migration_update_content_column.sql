-- Migration script to increase content column size for image support
-- Run this script in your MySQL database

USE classroom_support;

-- Modify the content column to support larger data (base64 images)
ALTER TABLE chat_messages 
MODIFY COLUMN content LONGTEXT NOT NULL;

-- Verify the change
DESCRIBE chat_messages;
