-- CSE 414 	Autumn 2018
-- Homework3 - Q6
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- List the names of carriers that operate flights from Seattle to San 
-- Francisco, CA. Return each carrier's name only once. Use a nested query
-- to answer this question. (7 points)

-- Name the output column carrier.

-- [Output relation cardinality: 4]

SELECT DISTINCT C.name	AS carrier
FROM	Carriers	AS C
WHERE	C.cid IN (
		SELECT DISTINCT F.carrier_id
		FROM 	Flights AS F
		WHERE 	F.origin_city = '"Seattle WA"' AND
			F.dest_city = '"San Francisco CA"'
		);
-- Number of rows: 4 
-- Time: 4 seconds
-- First 20 rows:
-- Alaska Airlines Inc.
-- SkyWest Airlines Inc.
-- United Air Lines Inc.
-- Virgin America
