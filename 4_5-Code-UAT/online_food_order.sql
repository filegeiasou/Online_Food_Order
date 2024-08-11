DROP DATABASE IF EXISTS Online_Food_Order_Delivery;
CREATE DATABASE Online_Food_Order_Delivery;
USE Online_Food_Order_Delivery;

CREATE TABLE User (
    USERNAME VARCHAR(50) PRIMARY KEY,
    PASSWORD VARCHAR(50)
);

CREATE TABLE Customer (
    ID INT PRIMARY KEY,
    USERNAME VARCHAR(50),
    PASSWORD VARCHAR(50),
    NAME VARCHAR(50),
    ADDRESS VARCHAR(50),
    FOREIGN KEY (USERNAME) REFERENCES User(USERNAME)
    -- FOREIGN KEY (PASSWORD) REFERENCES User(PASSWORD) 
);

CREATE TABLE Restaurant (
    ID INT PRIMARY KEY,
    USERNAME VARCHAR(50),
    PASSWORD VARCHAR(50),
    NAME VARCHAR(50),
    LOCATION VARCHAR(50),
    CUISINE_TYPE VARCHAR(50),
    RATING FLOAT(2,2),
    FOREIGN KEY (USERNAME) REFERENCES User(USERNAME)
    -- FOREIGN KEY (PASSWORD) REFERENCES User(PASSWORD)
);

CREATE TABLE Driver (
    ID INT PRIMARY KEY,
    USERNAME VARCHAR(50),
    PASSWORD VARCHAR(50),
    NAME VARCHAR(50),
    PHONE_NUMBER VARCHAR(50),
    FOREIGN KEY (USERNAME) REFERENCES User(USERNAME)
    -- FOREIGN KEY (PASSWORD) REFERENCES User(PASSWORD)
);

CREATE TABLE Administrator (
    ID INT PRIMARY KEY,
    USERNAME VARCHAR(50),
    PASSWORD VARCHAR(50),
    NAME VARCHAR(50),
    FOREIGN KEY (USERNAME) REFERENCES User(USERNAME)
    -- FOREIGN KEY (PASSWORD) REFERENCES User(PASSWORD)
);

INSERT INTO User
VALUES 
('admin', 'admin1234'),
('frottori', 'frosso1234'),
('tsosmi', 'dimitris1234'),
('filegeiasou', 'aggelos1234');

SELECT * FROM USER;
