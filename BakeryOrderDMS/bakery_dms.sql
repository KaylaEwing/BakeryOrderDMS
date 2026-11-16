-- =====================================================================
-- Bakery Order Management System - sample database (Phase 4)
-- Name:   Kayla Ewing
-- Course: CEN 3024C - Software Development I
-- Date:   November 13, 2026
--
-- Creates the database "bakery_dms" with one table, "orders", and fills
-- it with 25 sample orders. Run it once before starting the program:
--   MySQL Workbench: File > Open SQL Script > bakery_dms.sql, then click
--                    the lightning bolt (Execute).
--   Command line:    mysql -u root -p < bakery_dms.sql
--
-- Running it again resets the table to these 25 orders.
-- The program asks the user for the server address, username and password;
-- none of those are stored here or in the Java code.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS bakery_dms
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE bakery_dms;

DROP TABLE IF EXISTS orders;

-- One row per cake order. The CHECK rules repeat the program's validation
-- rules, so bad data cannot be saved even from outside the program
-- (CHECK rules are enforced by MySQL 8.0.16 and newer).
CREATE TABLE orders (
    order_id             INT           NOT NULL,               -- unique 4-digit order number
    customer_name        VARCHAR(30)   NOT NULL,               -- customer's full name
    phone_number         CHAR(12)      NOT NULL,               -- stored as 407-555-0142
    cake_type            VARCHAR(20)   NOT NULL,               -- Chocolate, Vanilla, Red Velvet or Carrot
    quantity             INT           NOT NULL,               -- number of cakes, 1-50
    unit_price           DECIMAL(7,2)  NOT NULL,               -- price of one cake in dollars
    pickup_date          DATE          NOT NULL,               -- day the customer collects the order
    is_picked_up         BOOLEAN       NOT NULL DEFAULT FALSE, -- TRUE once collected
    special_instructions VARCHAR(100)  NOT NULL DEFAULT '',    -- optional notes for the baker
    PRIMARY KEY (order_id),
    CONSTRAINT chk_order_id  CHECK (order_id BETWEEN 1000 AND 9999),
    CONSTRAINT chk_quantity  CHECK (quantity BETWEEN 1 AND 50),
    CONSTRAINT chk_price     CHECK (unit_price > 0 AND unit_price <= 1000),
    CONSTRAINT chk_cake_type CHECK (cake_type IN ('Chocolate', 'Vanilla', 'Red Velvet', 'Carrot'))
);

-- Speeds up the Daily Production Report, which searches by pickup date.
CREATE INDEX idx_orders_pickup ON orders (pickup_date, is_picked_up);

INSERT INTO orders
    (order_id, customer_name, phone_number, cake_type, quantity, unit_price, pickup_date, is_picked_up, special_instructions)
VALUES
    (1001, 'Mia Chen', '407-555-0101', 'Chocolate', 2, 25.00, '2027-02-13', FALSE, 'Write Happy Valentine''s Day in red icing'),
    (1002, 'Jin Park', '407-555-0102', 'Vanilla', 1, 22.00, '2027-02-13', FALSE, ''),
    (1003, 'Ana Ruiz', '321-555-0103', 'Red Velvet', 3, 28.00, '2027-02-13', FALSE, 'Heart shaped, nut-free'),
    (1004, 'Leo Grant', '407-555-0142', 'Chocolate', 1, 25.00, '2027-02-13', FALSE, 'Add pink sprinkles'),
    (1005, 'Priya Shah', '689-555-0105', 'Carrot', 2, 24.00, '2027-02-13', TRUE, 'Cream cheese frosting only'),
    (1006, 'Marcus Bell', '407-555-0106', 'Red Velvet', 1, 28.00, '2027-02-14', FALSE, 'Write Be Mine'),
    (1007, 'Sofia Alvarez', '321-555-0107', 'Vanilla', 4, 22.00, '2027-01-08', FALSE, 'Birthday party for 40 guests'),
    (1008, 'Daniel O''Brien', '407-555-0108', 'Chocolate', 1, 25.00, '2027-01-09', FALSE, ''),
    (1009, 'Grace Kim', '689-555-0109', 'Carrot', 1, 24.00, '2027-01-15', FALSE, 'No walnuts - allergy'),
    (1010, 'Omar Haddad', '407-555-0110', 'Red Velvet', 2, 28.00, '2027-01-15', FALSE, 'Gold leaf on top'),
    (1011, 'Emily Johnson', '321-555-0111', 'Vanilla', 3, 22.00, '2027-01-22', FALSE, 'Blue and white for a baby shower'),
    (1012, 'Tyrone Williams', '407-555-0112', 'Chocolate', 5, 25.00, '2027-01-29', FALSE, 'Office retirement party'),
    (1013, 'Hannah Lee', '689-555-0113', 'Vanilla', 1, 22.00, '2027-02-05', FALSE, 'Gluten-free if possible'),
    (1014, 'Carlos Mendez', '407-555-0114', 'Carrot', 2, 24.00, '2027-02-05', FALSE, ''),
    (1015, 'Aisha Patel', '321-555-0115', 'Chocolate', 2, 25.00, '2027-02-12', FALSE, 'Write Congrats Aisha'),
    (1016, 'Ben Carter', '407-555-0116', 'Red Velvet', 1, 28.00, '2027-02-19', FALSE, ''),
    (1017, 'Chloe Nguyen', '689-555-0117', 'Vanilla', 2, 22.00, '2027-02-26', FALSE, 'Lavender buttercream'),
    (1018, 'Isaac Rivera', '407-555-0118', 'Chocolate', 3, 25.00, '2027-03-05', FALSE, 'Soccer team celebration'),
    (1019, 'Julia Rossi', '321-555-0119', 'Carrot', 1, 24.00, '2027-03-12', FALSE, ''),
    (1020, 'Kevin Brooks', '407-555-0120', 'Red Velvet', 2, 28.00, '2027-03-14', FALSE, 'Pi Day theme'),
    (1021, 'Lauren Scott', '689-555-0121', 'Vanilla', 1, 22.00, '2027-03-17', FALSE, 'Green sprinkles for St. Patrick''s Day'),
    (1022, 'Mohammed Ali', '407-555-0122', 'Chocolate', 2, 25.00, '2027-03-20', FALSE, ''),
    (1023, 'Natalie Brown', '321-555-0123', 'Carrot', 3, 24.00, '2027-03-27', FALSE, 'Easter bunny decorations'),
    (1024, 'Oscar Diaz', '407-555-0124', 'Red Velvet', 1, 28.00, '2027-04-03', FALSE, 'Write Happy 30th Oscar'),
    (1025, 'Rachel Green', '689-555-0125', 'Vanilla', 2, 22.00, '2027-04-10', FALSE, 'Rainbow layers inside');

-- Quick check: should show 25 orders.
SELECT COUNT(*) AS orders_loaded FROM orders;
