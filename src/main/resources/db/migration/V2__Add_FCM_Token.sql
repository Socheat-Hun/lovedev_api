-- Add FCM token columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token_updated_at TIMESTAMP;

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_fcm_token ON users(fcm_token);