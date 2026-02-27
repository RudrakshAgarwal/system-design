-- 1. Create Databases
CREATE DATABASE IF NOT EXISTS flight_service_db;
CREATE DATABASE IF NOT EXISTS booking_service_db;
CREATE DATABASE IF NOT EXISTS payment_service_db;

-- 2. Create a specific user for Microservices (Bypasses root issues)
-- We use CREATE USER IF NOT EXISTS to avoid crashes on restart
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'password';

-- 3. Grant full permissions to this user
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%' WITH GRANT OPTION;

-- 4. Apply changes
FLUSH PRIVILEGES;