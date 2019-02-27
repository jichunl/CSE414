-- CSE 414 	Fall 2018
-- Homework1 - Q7
-- Name: Jichun Li
-- ID:	1531264

.header on

-- Write a SQL query that returns all restaurants that are within and including 
-- 10 mins from your house.

SELECT *
FROM MyRestaurants
WHERE Distance <= 10;
