CREATE DATABASE "RideVault";

CREATE TABLE Rider (
    Rider_Id SERIAL PRIMARY KEY,
    FirstName VARCHAR(100) NOT NULL,
    MiddleName VARCHAR(100),
    LastName VARCHAR(100) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    Phone_No VARCHAR(20) UNIQUE NOT NULL,
    Password_Hash VARCHAR(255) NOT NULL,
    Is_Verified BOOLEAN DEFAULT FALSE,
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Driver (
    Driver_Id SERIAL PRIMARY KEY,
    FirstName VARCHAR(100) NOT NULL,
    MiddleName VARCHAR(100),
    LastName VARCHAR(100) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    Phone_No VARCHAR(20) UNIQUE NOT NULL,
    Password_hash VARCHAR(255) NOT NULL,
    License_No VARCHAR(100) UNIQUE NOT NULL,
    Avg_Rating DECIMAL(3, 2) DEFAULT 0.00,
    Total_Rides INTEGER DEFAULT 0,
    Is_Verified BOOLEAN DEFAULT FALSE,
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE FARE_RULES (
    Fare_Id SERIAL PRIMARY KEY,
    Vehicle_Category VARCHAR(50) NOT NULL,
    Base_Fare DECIMAL(10, 2) NOT NULL,
    Per_Km_Rate DECIMAL(10, 2) NOT NULL,
    Per_Min_Rate DECIMAL(10, 2) NOT NULL,
    Min_Fare DECIMAL(10, 2) NOT NULL
);

CREATE TABLE SURGE_PRICING (
    Surge_Id SERIAL PRIMARY KEY,
    Zone_Name VARCHAR(100) NOT NULL,
    Lat_Min DECIMAL(9, 6) NOT NULL,
    Lat_Max DECIMAL(9, 6) NOT NULL,
    Lng_Min DECIMAL(9, 6) NOT NULL,
    Lng_Max DECIMAL(9, 6) NOT NULL,
    Day_Of_Week VARCHAR(15) NOT NULL,
    Start_Time TIME NOT NULL,
    End_Time TIME NOT NULL,
    Multiplier DECIMAL(3, 2) NOT NULL,
    Is_Active BOOLEAN DEFAULT TRUE
);

CREATE TABLE Offers (
    offer_id SERIAL PRIMARY KEY,
    Promo_Code VARCHAR(50) UNIQUE NOT NULL,
    Discount_Pct DECIMAL(5, 2),
    Flat_Discount DECIMAL(10, 2),
    Max_Discount DECIMAL(10, 2) NOT NULL,
    Min_Ride_Value DECIMAL(10, 2) NOT NULL,
    Valid_From TIMESTAMP NOT NULL,
    Valid_Until TIMESTAMP NOT NULL,
    Usage_Limit INTEGER NOT NULL,
    Total_Used INTEGER DEFAULT 0,
    Is_Active BOOLEAN DEFAULT TRUE
);

CREATE TABLE VEHICLE (
    Vehicle_Id SERIAL PRIMARY KEY,
    Driver_Id INTEGER NOT NULL,
    Model VARCHAR(100) NOT NULL,
    Year INTEGER NOT NULL,
    Plate_No VARCHAR(50) UNIQUE NOT NULL,
    Category VARCHAR(50) NOT NULL,
    Is_Active BOOLEAN DEFAULT TRUE,
    Is_Verified BOOLEAN DEFAULT FALSE,
    insurance_policy_no VARCHAR(100) NOT NULL,
    insurance_expiry_date DATE NOT NULL,
    CONSTRAINT fk_vehicle_driver FOREIGN KEY (Driver_Id) REFERENCES Driver(Driver_Id) ON DELETE CASCADE
);

CREATE TABLE WALLET (
    Wallet_Id SERIAL PRIMARY KEY,
    Driver_Id INTEGER UNIQUE NOT NULL,
    Balance DECIMAL(12, 2) DEFAULT 0.00,
    CONSTRAINT fk_wallet_driver FOREIGN KEY (Driver_Id) REFERENCES Driver(Driver_Id) ON DELETE CASCADE
);

CREATE TABLE Driver_Session (
    Session_Id SERIAL PRIMARY KEY,
    Driver_Id INTEGER UNIQUE NOT NULL,
    Status VARCHAR(50) NOT NULL,
    Last_Known_Lat DECIMAL(9, 6),
    Last_Known_Lng DECIMAL(9, 6),
    CONSTRAINT fk_session_driver FOREIGN KEY (Driver_Id) REFERENCES Driver(Driver_Id) ON DELETE CASCADE
);

CREATE TABLE Ride (
    Ride_Id SERIAL PRIMARY KEY,
    Rider_Id INTEGER NOT NULL,
    Driver_Id INTEGER,
    Vehicle_Id INTEGER,
    Fare_Id INTEGER,
    Surge_Id INTEGER,
    offer_id INTEGER,
    Status VARCHAR(50) NOT NULL,
    Requested_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Completed_At TIMESTAMP,
    Cancelled_At TIMESTAMP,
    Cancel_Reason VARCHAR(255),
    Final_Fare DECIMAL(10, 2),
    Driver_Earning DECIMAL(10, 2),
    Distance_Km DECIMAL(8, 2),
    Duration_Min INTEGER,
    Pickup_Address TEXT NOT NULL,
    Pickup_Lat DECIMAL(9, 6) NOT NULL,
    Pickup_Lng DECIMAL(9, 6) NOT NULL,
    Dropoff_Address TEXT NOT NULL,
    Dropoff_Lat DECIMAL(9, 6) NOT NULL,
    Dropoff_Lng DECIMAL(9, 6) NOT NULL,
    Rated_By_Rider BOOLEAN DEFAULT FALSE,
    Rated_By_Driver BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_ride_rider FOREIGN KEY (Rider_Id) REFERENCES Rider(Rider_Id),
    CONSTRAINT fk_ride_driver FOREIGN KEY (Driver_Id) REFERENCES Driver(Driver_Id),
    CONSTRAINT fk_ride_vehicle FOREIGN KEY (Vehicle_Id) REFERENCES VEHICLE(Vehicle_Id),
    CONSTRAINT fk_ride_fare FOREIGN KEY (Fare_Id) REFERENCES FARE_RULES(Fare_Id),
    CONSTRAINT fk_ride_surge FOREIGN KEY (Surge_Id) REFERENCES SURGE_PRICING(Surge_Id),
    CONSTRAINT fk_ride_offer FOREIGN KEY (offer_id) REFERENCES Offers(offer_id)
);

CREATE TABLE Payment (
    Payment_Id SERIAL PRIMARY KEY,
    Ride_Id INTEGER UNIQUE NOT NULL,
    Rider_Id INTEGER NOT NULL,
    Method VARCHAR(50) NOT NULL,
    Amount DECIMAL(10, 2) NOT NULL,
    Status VARCHAR(50) NOT NULL,
    Transaction_Ref VARCHAR(100) UNIQUE,
    Paid_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_ride FOREIGN KEY (Ride_Id) REFERENCES Ride(Ride_Id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_rider FOREIGN KEY (Rider_Id) REFERENCES Rider(Rider_Id)
);

CREATE TABLE WALLET_TRANSACTIONS (
    Txn_Id SERIAL PRIMARY KEY,
    Wallet_Id INTEGER NOT NULL,
    Ride_Id INTEGER,
    Movement VARCHAR(20) NOT NULL CHECK (Movement IN ('CREDIT', 'DEBIT')),
    Purpose VARCHAR(100) NOT NULL,
    Amount DECIMAL(10, 2) NOT NULL,
    Balance_After DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_wallet_txn_wallet FOREIGN KEY (Wallet_Id) REFERENCES WALLET(Wallet_Id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_txn_ride FOREIGN KEY (Ride_Id) REFERENCES Ride(Ride_Id)
);

CREATE TABLE Rating (
    Ride_Id INTEGER PRIMARY KEY,
    Rating_For_Driver INTEGER CHECK (Rating_For_Driver BETWEEN 1 AND 5),
    Driver_Comment TEXT,
    Rating_For_Rider INTEGER CHECK (Rating_For_Rider BETWEEN 1 AND 5),
    Rider_Comment TEXT,
    CONSTRAINT fk_rating_ride FOREIGN KEY (Ride_Id) REFERENCES Ride(Ride_Id) ON DELETE CASCADE
);

CREATE TABLE Ride_Status_Log (
    Log_Id SERIAL PRIMARY KEY,
    Ride_Id INTEGER NOT NULL,
    Status VARCHAR(50) NOT NULL,
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Change_By VARCHAR(50) NOT NULL,
    CONSTRAINT fk_status_log_ride FOREIGN KEY (Ride_Id) REFERENCES Ride(Ride_Id) ON DELETE CASCADE
);

CREATE INDEX idx_ride_rider ON Ride(Rider_Id);
CREATE INDEX idx_ride_driver ON Ride(Driver_Id);
CREATE INDEX idx_ride_status ON Ride(Status);
CREATE INDEX idx_vehicle_category ON VEHICLE(Category);
CREATE INDEX idx_wallet_txn_wallet ON WALLET_TRANSACTIONS(Wallet_Id);
CREATE INDEX idx_payment_rider ON Payment(Rider_Id);
