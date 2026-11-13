-- ParkingDB.sql  (Microsoft SQL Server)
-- Run it once in SSMS or Azure Data Studio. It is safe to run again:
-- nothing is dropped and nothing is inserted twice.
-- GO is a batch separator of those tools, so do not run this file through JDBC.

IF DB_ID('ParkingDB') IS NULL
    CREATE DATABASE ParkingDB;
GO

USE ParkingDB;
GO

-- users: login accounts. role decides which side of the web page they see.
IF OBJECT_ID('dbo.users', 'U') IS NULL
CREATE TABLE dbo.users (
    id            INT IDENTITY(1, 1) PRIMARY KEY,         -- start at 1, step 1
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,                  -- the password as typed (no hashing); the column keeps its name, rename it if you like
    role          VARCHAR(20)  NOT NULL DEFAULT 'user'
                  CONSTRAINT ck_users_role CHECK (role IN ('user', 'monitor'))
);
GO

-- parking_lots: the Java code finds a lot by its name, so the name must be unique.
IF OBJECT_ID('dbo.parking_lots', 'U') IS NULL
CREATE TABLE dbo.parking_lots (
    id      INT IDENTITY(1, 1) PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(300) NOT NULL
);
GO

-- park_spaces: one row per slot. A slot code can repeat across lots but not inside one lot.
IF OBJECT_ID('dbo.park_spaces', 'U') IS NULL
CREATE TABLE dbo.park_spaces (
    id         INT IDENTITY(1, 1) PRIMARY KEY,
    lot_id     INT         NOT NULL,
    space_code VARCHAR(20) NOT NULL,                      -- e.g. A1, A2, A3
    is_taken   BIT         NOT NULL DEFAULT 0,            -- 0 = free, 1 = taken
    CONSTRAINT fk_spaces_lot FOREIGN KEY (lot_id)
        REFERENCES dbo.parking_lots(id) ON DELETE CASCADE, -- deleting a lot deletes its slots
    CONSTRAINT uq_spaces_lot_code UNIQUE (lot_id, space_code)
);
GO

-- cars: balance is INT because Car.balance and the fare are int in Java.
IF OBJECT_ID('dbo.cars', 'U') IS NULL
CREATE TABLE dbo.cars (
    id           INT IDENTITY(1, 1) PRIMARY KEY,
    plate_number VARCHAR(20)  NOT NULL UNIQUE,            -- two cars never share a plate
    owner_name   VARCHAR(100) NOT NULL,
    car_type     VARCHAR(50)  NULL,
    balance      INT          NOT NULL DEFAULT 0,
    user_id      INT          NULL,
    CONSTRAINT fk_cars_user FOREIGN KEY (user_id)
        REFERENCES dbo.users(id) ON DELETE SET NULL        -- the car stays when its user is deleted
);
GO

-- sessions: one row per parking visit. end_time and fare stay NULL while the car is parked.
IF OBJECT_ID('dbo.sessions', 'U') IS NULL
CREATE TABLE dbo.sessions (
    id         INT IDENTITY(1, 1) PRIMARY KEY,
    space_id   INT       NOT NULL,
    car_id     INT       NOT NULL,
    start_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    end_time   DATETIME2 NULL,
    fare       INT       NULL,
    CONSTRAINT fk_sessions_space FOREIGN KEY (space_id)
        REFERENCES dbo.park_spaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_sessions_car FOREIGN KEY (car_id)
        REFERENCES dbo.cars(id) ON DELETE CASCADE
);
GO

-- A car can have only one open session, and a slot can hold only one open session.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ux_sessions_open_car')
    CREATE UNIQUE INDEX ux_sessions_open_car
        ON dbo.sessions(car_id) WHERE end_time IS NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ux_sessions_open_space')
    CREATE UNIQUE INDEX ux_sessions_open_space
        ON dbo.sessions(space_id) WHERE end_time IS NULL;
GO

-- Sample data, only when the tables are empty.
IF NOT EXISTS (SELECT 1 FROM dbo.parking_lots)
BEGIN
    INSERT INTO dbo.parking_lots (name, address)
    VALUES ('Lot A', 'Sample address 1'),
           ('Lot B', 'Sample address 2');

    INSERT INTO dbo.park_spaces (lot_id, space_code)
    SELECT l.id, v.code
    FROM dbo.parking_lots l
    CROSS JOIN (VALUES ('A1'), ('A2'), ('A3'), ('A4'), ('A5')) AS v(code)
    WHERE l.name = 'Lot A';

    INSERT INTO dbo.park_spaces (lot_id, space_code)
    SELECT l.id, v.code
    FROM dbo.parking_lots l
    CROSS JOIN (VALUES ('B1'), ('B2'), ('B3'), ('B4'), ('B5')) AS v(code)
    WHERE l.name = 'Lot B';
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.cars)
    INSERT INTO dbo.cars (plate_number, owner_name, car_type, balance)
    VALUES ('TEST-001', 'Test Owner 1', 'Sedan', 50000),
           ('TEST-002', 'Test Owner 2', 'SUV',   50000);
GO

-- Users are not created here. Create them from Java with UserDao.createUser,
-- then link a car to a user with:
--   UPDATE dbo.cars SET user_id = <users.id> WHERE plate_number = 'TEST-001';