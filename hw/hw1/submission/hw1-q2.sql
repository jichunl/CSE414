-- CSE 414 	Fall 2018
-- Homework1 - Q2
-- Name: Jichun Li
-- ID:	1531264

.header on
.mode col

-- Next, you will create a table with attributes of types integer, varchar, 
-- 	date, and Boolean. However, SQLite does not have date and Boolean: you will 
-- 	use varchar and int instead. Some notes:

-- 0 (false) and 1 (true) are the values used to interpret Booleans.
-- Date strings in SQLite are in the form: 'YYYY-MM-DD'.
-- 	Examples of valid date strings include: 
--	'1988-01-15', '0000-12-31', and '2011-03-28'.
-- 	Examples of invalid date strings include: 
--	'11-11-01', '1900-1-20', '2011-03-5', and '2011-03-50'.
-- Examples of date operations on date strings (feel free to try them):
-- 	select date('2011-03-28');
-- 	select date('now');
-- 	select date('now', '-5 year');
-- 	select date('now', '-5 year', '+24 hour');
-- 	select case when date('now') < date('2011-12-09') 
--		then 'Taking classes' 
--		when date('now') < date('2011-12-16') 
--		then 'Exams' else 'Vacation' end; 
--	What does this query do? (no need to turn in your answer)
-- Create a table called MyRestaurants with the following attributes (you can 
--	pick your own names for the attributes, just make sure it is clear which 
--	one is for which): 
-- Name of the restaurant: a varchar field
-- Type of food they make: a varchar field
-- Distance (in minutes) from your house: an int
-- Date of your last visit: a varchar field, interpreted as date
-- Whether you like it or not: an int, interpreted as a Boolean

CREATE TABLE MyRestaurants (
	Name varchar,
	Food varchar,
	Distance int,
	LastVisit varchar(10),
	Liked int
);


