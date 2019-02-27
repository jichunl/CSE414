-- CSE 414 	Autumn 2018
-- Homework3 - Q3
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- For each origin city, find the percentage of departing flights shorter than
-- 3 hours. For this question, treat flights with NULL actual_time values as 
-- longer than 3 hours. (15 points)

-- Name the output columns origin_city and percentage. Order by percentage 
-- value. Be careful to handle cities without any flights shorter than 3 
-- hours. We will accept both 0 and NULL as the result for those cities.

-- [Output relation cardinality: 327]

SELECT	DISTINCT Flights_2.F2_origin_city AS city,
	ISNULL(100.0 * Flights_1.F1_total/Flights_2.F2_total, 0) AS percentage
FROM	(
	SELECT	F1.origin_city	AS F1_origin_city,
		COUNT(*)	AS F1_total
	FROM Flights AS F1
	WHERE ISNULL(F1.actual_time,200) < 180
	GROUP BY F1.origin_city
	) AS Flights_1 right outer join (
	SELECT	F2.origin_city	AS F2_origin_city,
		COUNT(*)	AS F2_total
	FROM Flights AS F2
	GROUP BY F2.origin_city
	) AS Flights_2
ON Flights_1.F1_origin_city = Flights_2.F2_origin_city
ORDER BY percentage;

-- Number of rows: 327 
-- Time: 16 seconds
-- First 20 rows:
-- "Guam TT",0.000000000000
-- "Pago Pago TT",0.000000000000
-- "Aguadilla PR",29.433962264150
-- "Anchorage AK",32.146037399821
-- "San Juan PR",33.890360709190
-- "Charlotte Amalie VI",40.000000000000
-- "Ponce PR",41.935483870967
-- "Fairbanks AK",50.691244239631
-- "Kahului HI",53.664998528113
-- "Honolulu HI",54.908808692277
-- "San Francisco CA",56.307656826568
-- "Los Angeles CA",56.604107648725
-- "Seattle WA",57.755416553349
-- "Long Beach CA",62.454116413214
-- "Kona HI",63.282107574094
-- "New York NY",63.481519772551
-- "Las Vegas NV",65.163009288383
-- "Christiansted VI",65.333333333333
-- "Newark NJ",67.137355584082
-- "Worcester MA",67.741935483870
