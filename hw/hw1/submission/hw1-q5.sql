-- CSE 414 	Fall 2018
-- Homework1 - Q5
-- Name: Jichun Li
-- ID:	1531264

.header on

-- Write a SQL query that returns only the name and distance of all restaurants 
-- within and including 20 minutes of your house. The query should list the 
-- restaurants in alphabetical order of names.

SELECT Name, Distance 
FROM MyRestaurants
WHERE Distance <= 20
ORDER BY Name ASC;



