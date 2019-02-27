-- CSE 414 	Autumn 2018
-- Homework3 - Q2
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- Find all origin cities that only serve flights shorter than 3 hours. You can
-- assume that flights with NULL actual_time are not 3 hours or more. (15 points)

-- Name the output column city and sort them. List each city only once in the result.

-- [Output relation cardinality: 109]


SELECT	DISTINCT F.origin_city 	AS city
FROM	Flights AS F
GROUP BY F.origin_city
HAVING MAX(ISNULL(F.actual_time,0)) < 180
ORDER BY F.origin_city;

-- Number of rows: 109 
-- Time: 9 seconds
-- First 20 rows:
-- "Aberdeen SD"
-- "Abilene TX"
-- "Alpena MI"
-- "Ashland WV"
-- "Augusta GA"
-- "Barrow AK"
-- "Beaumont/Port Arthur TX"
-- "Bemidji MN"
-- "Bethel AK"
-- "Binghamton NY"
-- "Brainerd MN"
-- "Bristol/Johnson City/Kingsport TN"
-- "Butte MT"
-- "Carlsbad CA"
-- "Casper WY"
-- "Cedar City UT"
-- "Chico CA"
-- "College Station/Bryan TX"
-- "Columbia MO"
-- "Columbus GA"
