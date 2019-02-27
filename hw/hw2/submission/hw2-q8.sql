-- CSE 414 	Autumn 2018
-- Homework2 - Q8
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- Compute the total departure delay of each airline across all flights.
-- Name the output columns name and delay, in that order.

SELECT 	C.name AS 'name', 
	SUM(F.departure_delay) AS 'delay'
FROM 	FLIGHTS AS F,
	CARRIERS AS C
WHERE	F.carrier_id = C.cid
GROUP BY F.carrier_id;
-- number of rows: 22 
