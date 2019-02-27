-- CSE 414 	Autumn 2018
-- Homework2 - Q6
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- Find the maximum price of tickets between Seattle and New York, NY. Show the
-- maximum price for each airline separately. Name the output columns carrier 
-- and max_price, in that order.

SELECT 	C.name AS carrier, 
	MAX(F.price) AS max_price
FROM 	FLIGHTS AS F,
	CARRIERS AS C
WHERE 	F.carrier_id = C.cid
	AND (F.origin_city = 'Seattle WA'
	     	OR F.origin_city = 'New York NY')
	AND (F.dest_city = 'New York NY'
		OR F.dest_city = 'Seattle WA')
GROUP BY F.carrier_id;
-- number of rows: 3
