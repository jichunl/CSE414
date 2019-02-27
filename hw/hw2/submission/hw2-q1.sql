-- CSE 414 	Autumn 2018
-- Homework2 - Q1
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- List the distinct flight numbers of all flights from Seattle to Boston by 
-- Alaska Airlines Inc. on Mondays. Also notice that, in the database, the city
-- names include the state. So Seattle appears as Seattle WA.
-- Name the output column flight_num.

SELECT DISTINCT F.flight_num AS flight_num
FROM FLIGHTS AS F, CARRIERS AS C, WEEKDAYS AS W
WHERE F.carrier_id = C.cid
	AND F.day_of_week_id = W.did
	AND C.name = 'Alaska Airlines Inc.'
	AND F.origin_city = 'Seattle WA'
	AND F.dest_city = 'Boston MA'
	AND W.day_of_week = 'Monday';

-- number of rows: 3
