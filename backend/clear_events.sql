-- Clear Old Events Script
-- This script deletes all events from the database so they can be re-synced with correct date parsing

-- Connect to the database first:
-- psql -U postgres -d mail_assistant

-- Delete all events (this will force re-sync)
DELETE FROM job_event;

-- Verify deletion
SELECT COUNT(*) FROM job_event;

-- Expected output: 0

-- Exit psql
-- \q

-- After running this script:
-- 1. Go to the frontend (http://localhost:5173)
-- 2. Click "Sync Emails" button
-- 3. Wait for emails to be re-processed with correct dates
