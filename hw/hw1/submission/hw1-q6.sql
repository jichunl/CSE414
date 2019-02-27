-- CSE 414 	Fall 2018
-- Homework1 - Q6
-- Name: Jichun Li
-- ID:	1531264

.header on

-- Write a SQL query that returns all restaurants that you like, but have not
-- visited since more than 3 months ago.

SELECT *
FROM MyRestaurants
WHERE Liked = 1
	AND date(LastVisit) < date('now', '-3 month');
