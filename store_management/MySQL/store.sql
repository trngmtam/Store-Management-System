CREATE DATABASE RetailStore;

USE RetailStore;

CREATE TABLE Product (
    productID INT PRIMARY KEY ,
    productName VARCHAR(100) NOT NULL ,
    productDescription VARCHAR(500),
    productPrice DECIMAL(10,2)
);

CREATE TABLE Inventory (
    productID INT PRIMARY KEY ,
    quantity INT DEFAULT 0,
    location VARCHAR(50),
    lastUpdated TIMESTAMP,
    FOREIGN KEY (productID) REFERENCES Product(productID)
);

CREATE TABLE Account (
    customerID INT PRIMARY KEY,
    password VARCHAR(100),
    FOREIGN KEY (customerID) REFERENCES Customer(customerID)
);

-- Insert Products
INSERT INTO Product (productID, productName, productDescription, productPrice) VALUES
(1, 'Laptop', 'High-performance laptop', 999.99),
(2, 'Smartphone', 'Latest smartphone model', 699.99),
(3, 'Programming Book', 'Learn coding basics', 49.99);

-- Insert Inventory
INSERT INTO Inventory (productID, quantity, location, lastUpdated) VALUES
(1, 50, 'Warehouse A', CURRENT_TIMESTAMP),
(2, 100, 'Warehouse B', CURRENT_TIMESTAMP),
(3, 200, 'Warehouse A', CURRENT_TIMESTAMP);

-- Insert Account
INSERT INTO Account (customerID, password) VALUES
(1,'1'),
(2,'2');
