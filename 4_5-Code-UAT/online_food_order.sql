DROP DATABASE IF EXISTS Online_Food_Order_Delivery;
CREATE DATABASE Online_Food_Order_Delivery;

# Create a user for the database
CREATE USER online_food_order_user@localhost IDENTIFIED BY 'food';
GRANT ALL PRIVILEGES ON Online_Food_Order_Delivery.* TO online_food_order_user@localhost;

USE Online_Food_Order_Delivery;

CREATE TABLE User (
	ID INT PRIMARY KEY AUTO_INCREMENT,
    USERNAME VARCHAR(50),
    PASSWORD VARCHAR(50),
    EMAIL VARCHAR(50) UNIQUE,
    USER_TYPE VARCHAR(50)
);

CREATE TABLE Customer (
    ID INT PRIMARY KEY AUTO_INCREMENT,
    USERNAME VARCHAR(50) UNIQUE,
    PASSWORD VARCHAR(50),
    ADDRESS VARCHAR(50),
    FOREIGN KEY (ID) REFERENCES User(ID) ON DELETE CASCADE
);

CREATE TABLE Restaurant (
    ID INT PRIMARY KEY AUTO_INCREMENT,
    USERNAME VARCHAR(50) UNIQUE,
    PASSWORD VARCHAR(50),
    NAME VARCHAR(50),
    LOCATION VARCHAR(50),
    CUISINE_TYPE VARCHAR(50),
    RATING FLOAT(10,2),
    FOREIGN KEY (ID) REFERENCES User(ID) ON DELETE CASCADE
);

CREATE TABLE Driver (
    ID INT PRIMARY KEY AUTO_INCREMENT,
    USERNAME VARCHAR(50) UNIQUE,
    PASSWORD VARCHAR(50),
    PHONE_NUMBER VARCHAR(50),
    FOREIGN KEY (ID) REFERENCES User(ID) ON DELETE CASCADE
);

CREATE TABLE Administrator (
    ID INT PRIMARY KEY AUTO_INCREMENT,
    USERNAME VARCHAR(50) UNIQUE,
    PASSWORD VARCHAR(50),
    FOREIGN KEY (ID) REFERENCES User(ID) ON DELETE CASCADE
);

CREATE TABLE Menu(
    ID INT PRIMARY KEY AUTO_INCREMENT,
    RESTAURANT_ID INT,
    NAME VARCHAR(50),
    PRICE FLOAT(10,2),
    CATEGORY VARCHAR(50),
    FOREIGN KEY (RESTAURANT_ID) REFERENCES Restaurant(ID) ON DELETE CASCADE
);

CREATE TABLE Orders(
    ID INT PRIMARY KEY AUTO_INCREMENT,
    CUSTOMER_ID INT,
    RESTAURANT_ID INT,
    DRIVER_ID INT,
    QUANTITY INT,
    ITEMS TEXT,
    TOTAL_PRICE FLOAT(10,2),
    STATUS VARCHAR(50),
    FOREIGN KEY (CUSTOMER_ID) REFERENCES Customer(ID) ON DELETE CASCADE,
    FOREIGN KEY (RESTAURANT_ID) REFERENCES Restaurant(ID)ON DELETE CASCADE,
    FOREIGN KEY (DRIVER_ID) REFERENCES Driver(ID) ON DELETE CASCADE
);

DELIMITER //
CREATE TRIGGER capitalize
BEFORE INSERT ON CUSTOMER
FOR EACH ROW
BEGIN
    SET NEW.ADDRESS = CONCAT(UPPER(SUBSTRING(NEW.ADDRESS, 1, 1)), LOWER(SUBSTRING(NEW.ADDRESS, 2)));
END //
DELIMITER ;

INSERT INTO User(USERNAME, PASSWORD, EMAIL, USER_TYPE) VALUES 
('admin', 'admin1234', "admin@gmail.com", "Admin"),
('manos', 'manos1234', "manos@gmail.com", "Restaurant"),
('aggelos', 'aggelos1234', "aggelos@gmail.com", "Restaurant");
-- ('frottori', 'frosso1234'),
-- ('tsosmi', 'dimitris1234'),
-- ('filegeiasou', 'aggelos1234');

# Admins will probably just be in the database and can only sign in.
INSERT INTO ADMINISTRATOR(USERNAME, PASSWORD) VALUES ("admin", "admin1234");

# ΕΝΔΕΙΚΤΙΚΑ
INSERT INTO Restaurant(USERNAME, PASSWORD, NAME, LOCATION, CUISINE_TYPE, RATING) VALUES ("manos", "manos1234", "Misafir", "Athens", "Turkish", 4);
INSERT INTO Restaurant(USERNAME, PASSWORD, NAME, LOCATION, CUISINE_TYPE, RATING) VALUES ("ellinas", "ellinas1234", "Elliniko", "Athens", "Greek", 4.5);
INSERT INTO RESTAURANT (USERNAME, PASSWORD, NAME, LOCATION, CUISINE_TYPE, RATING) VALUES ("frosso", "kim1234", "Shiraki", "Athens", "Japanese", 4.5);

# MENU FOR MISAFIR
INSERT INTO Menu(RESTAURANT_ID, NAME, PRICE, CATEGORY) 
VALUES 
(1, "Kebab", 5, "Main"),
(1, "Baklava", 3, "Dessert"),
(1, "Raki", 4, "Drinks"),
(1, "Kokoretsi", 6, "Main"),
(1, "Souvlaki", 4, "Main"),
(1, "Tzatziki", 2, "Appetizer"),
(1, "Moussaka", 5, "Main"),
(1, "Frappe", 3, "Drinks"),
(1, "Galaktoboureko", 4, "Dessert"),
(1, "Ouzo", 4, "Drinks"),
(1, "Pastitsio", 5, "Main"),
(1, "Saganaki", 3, "Appetizer"),
(1, "Loukoumades", 3, "Dessert"),
(1, "Tsipouro", 4, "Drinks");

# MENU FOR ELLINIKO
INSERT INTO Menu(RESTAURANT_ID, NAME, PRICE, CATEGORY)
VALUES
(2, "Chicken Souvlaki", 5, "Main"),
(2, "Pork Souvlaki", 5, "Main"),
(2, "Lamb Gyros", 6, "Main"),
(2, "Greek Salad", 5, "Appetizer"),
(2, "Spanakopita", 4, "Appetizer"),
(2, "Dolmades", 5, "Appetizer"),
(2, "Falafel Wrap", 5, "Main"),
(2, "Baklava", 3, "Dessert"),
(2, "Loukoumades", 4, "Dessert"),
(2, "Greek Yogurt with Honey", 3, "Dessert"),
(2, "Grilled Halloumi", 4, "Appetizer"),
(2, "Ouzo", 6, "Drinks"),
(2, "Greek Coffee", 3, "Drinks"),
(2, "Mythos Beer", 5, "Drinks");

# MENU FOR SHIRAKI
INSERT INTO Menu(RESTAURANT_ID, NAME, PRICE, CATEGORY)
VALUES
(3, "Sushi", 5, "Main"),
(3, "Sashimi", 6, "Main"),
(3, "Miso Soup", 3, "Appetizer"),
(3, "Edamame", 4, "Appetizer"),
(3, "Gyoza", 5, "Appetizer"),
(3, "Tempura", 6, "Appetizer"),
(3, "California Roll", 5, "Main"),
(3, "Dragon Roll", 6, "Main"),
(3, "Rainbow Roll", 7, "Main"),
(3, "Salmon Nigiri", 4, "Main"),
(3, "Tuna Nigiri", 5, "Main"),
(3, "Sake", 4, "Drinks"),
(3, "Sapporo Beer", 5, "Drinks"),
(3, "Green Tea", 3, "Drinks");

SELECT * FROM USER;
SELECT * FROM CUSTOMER;
SELECT * FROM DRIVER;
SELECT * FROM RESTAURANT;
SELECT * FROM MENU;
SELECT * FROM ORDERS;