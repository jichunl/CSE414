-- CSE 414 	Autumn 2018
-- Homework2 - Q7
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- Find the total capacity of all direct flights that fly between Seattle and 
-- San Francisco, CA on July 10th. Name the output column capacity.

SELECT SUM(F.capacity)
FROM 	FLIGHTS	AS F,
	MONTHS	AS M
WHERE	F.month_id = M.mid
	AND (F.origin_city = 'Seattle WA' OR F.origin_city = 'San Francisco CA')
	AND (F.dest_city = 'Seattle WA' OR F.dest_city = 'San Francisco CA')
	AND M.month = 'July'
	AND F.day_of_month = 10;
-- number of rows: 1
