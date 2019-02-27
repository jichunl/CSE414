-- CSE 414 	Fall 2018
-- Homework1 - Q4
-- Name: Jichun Li
-- ID:	1531264

.header on
 
-- Write a SQL query that returns all restaurants in your table. Experiment 
-- with a few of SQLite's output formats and show the command you use to format
-- the output along with your query:

-- print the results in comma-separated form

.mode csv
SELECT * FROM MyRestaurants;


-- print the results in list form, delimited by "|"

.mode list
.separator '|'

SELECT * FROM MyRestaurants;


-- print the results in column form, and make each column have width 15

.mode col
.width 15 15 15 15

SELECT * FROM MyRestaurants;

-- for each of the formats above, try printing/not printing the column headers with the results

.header off

.mode csv
SELECT * FROM MyRestaurants;


.mode list
.separator '|'

SELECT * FROM MyRestaurants;


.mode col
.width 15 15 15 15

SELECT * FROM MyRestaurants;
