-- CSE414 Autumn 2018
-- HW7 Part 2
-- Jichun Li 1531264

-- 1. Create a table in the database and load the data from the provided file 
-- 	into that table

CREATE TABLE Sales (
    name varchar,
    discount varchar,
    month varchar,
    price int
);

.seperator "\t"
.import mrFrumbleData.txt Sales


-- 2. Find all nontrivial functional dependencies in the database.

-- name → price
SELECT S.name AS name, count(DISTINCT S.price) AS distinctPrice
FROM Sales AS S
GROUP BY S.name
HAVING count(DISTINCT S.price) > 1;
-- Rows count: 0; The count of distinct price is 1.

-- month → discount
SELECT 	S.month AS month, 
	count(DISTINCT S.discount) AS distinctDiscount
FROM Sales AS S
GROUP BY S.month
HAVING count(DISTINCT S.discount) > 1;
-- Rows count: 0; The count of distinct discount is 1.


-- 3.Decompose the table in BCNF, and create SQL tables for the decomposed 
--	schema. Create keys and foreign keys where appropriate.

CREATE TABLE nameAndPrice (
    name text PRIMARY KEY,
    price int
);

CREATE TABLE monthAndDiscount (
    month text PRIMARY KEY,
    discount text
);

CREATE TABLE nameAndMonth(
    name text,
    month text,
    FOREIGN KEY(name) REFERENCES D1(name),
    FOREIGN KEY(month) REFERENCES D2(month)
);


-- 4.Populate your BCNF tables from Mr. Frumble's data.
INSERT INTO nameAndPrice
SELECT S.name AS name, S.price AS price
FROM Sales AS S;
SELECT COUNT(*) FROM nameAndPrice;
-- Rows Count: 36

INSERT INTO monthAndDiscount
SELECT S.month AS month, S.price AS discount
FROM Sales AS S;
SELECT COUNT(*) FROM monthAndDiscount;
-- Rows Count: 12

INSERT INTO nameAndMonth
SELECT S.month AS name, S.price AS month
FROM Sales AS S;
SELECT COUNT(*) FROM nameAndMonth;
-- Rows Count: 426

