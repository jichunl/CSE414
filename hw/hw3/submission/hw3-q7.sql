-- CSE 414 	Autumn 2018
-- Homework3 - Q7
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- Express the same query as above, but do so without using a nested query. 
-- Again, name the output column carrier. (8 points)


SELECT DISTINCT C.name AS carrier
FROM	Carriers AS C, Flights AS F
WHERE	F.origin_city = '"Seattle WA"' AND
	F.dest_city = '"San Francisco CA"' AND
	F.carrier_id = C.cid;

-- Number of rows: 4 
-- Time: 2 seconds
-- First 20 rows:
-- Alaska Airlines Inc.
-- SkyWest Airlines Inc.
-- United Air Lines Inc.
-- Virgin America
