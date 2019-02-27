-- CSE 414 	Autumn 2018
-- Homework2 - Q5
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- Find all airlines that had more than 0.5 percent of their flights out of
-- Seattle be canceled. Return the name of the airline and the percentage of 
-- canceled flight out of Seattle. Order the results by the percentage of 
-- canceled flights in ascending order. Name the output columns name and 
-- percent, in that order. 

SELECT 	C.name AS name,
	AVG(F.canceled)	AS percent
FROM 	FLIGHTS AS F,
	CARRIERS AS C
WHERE 	F.carrier_id = C.cid
	AND F.origin_city = 'Seattle WA'
GROUP BY C.cid
HAVING percent > 0.005
ORDER BY percent ASC;
-- number of rows: 6 
