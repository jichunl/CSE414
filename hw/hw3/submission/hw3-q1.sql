-- CSE 414 	Autumn 2018
-- Homework3 - Q1
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- For each origin city, find the destination city (or cities) with the longest
-- direct flight. By direct flight, we mean a flight with no intermediate stops.
-- Judge the longest flight in time, not distance. (15 points)

-- Name the output columns origin_city, dest_city, and time representing the the
-- flight time between them. Do not include duplicates of the same origin/
-- destination city pair. 

-- Order the result by origin_city and then dest_city.

-- [Output relation cardinality: 334 rows]


SELECT DISTINCT F.origin_city 	AS origin_city,
		F.dest_city 	AS dest_city,
		F.actual_time 	AS time
FROM 	Flights AS F, (
	SELECT 	F_sub.origin_city 	AS sub_origin_city, 
		MAX(F_sub.actual_time) 	AS sub_time
	FROM	Flights AS F_sub
	GROUP BY F_sub.origin_city
	) F_max
WHERE 	F.origin_city = F_max.sub_origin_city AND
	F.actual_time = F_max.sub_time
ORDER BY F.origin_city, F.dest_city;

-- Number of rows: 334
-- Time: 23 seconds
-- First 20 rows: 
-- ORIGIN_CITY,DEST_CITY,TIME
-- "Aberdeen SD","Minneapolis MN",106
-- "Abilene TX","Dallas/Fort Worth TX",111
-- "Adak Island AK","Anchorage AK",471
-- "Aguadilla PR","New York NY",368
-- "Akron OH","Atlanta GA",408
-- "Albany GA","Atlanta GA",243
-- "Albany NY","Atlanta GA",390
-- "Albuquerque NM","Houston TX",492
-- "Alexandria LA","Atlanta GA",391
-- "Allentown/Bethlehem/Easton PA","Atlanta GA",456
-- "Alpena MI","Detroit MI",80
-- "Amarillo TX","Houston TX",390
-- "Anchorage AK","Barrow AK",490
-- "Appleton WI","Atlanta GA",405
-- "Arcata/Eureka CA","San Francisco CA",476
-- "Asheville NC","Chicago IL",279
-- "Ashland WV","Cincinnati OH",84
-- "Aspen CO","Los Angeles CA",304
-- "Atlanta GA","Honolulu HI",649
-- "Atlantic City NJ","Fort Lauderdale FL",212
