-- CSE 414 	Autumn 2018
-- Homework2 - Q4
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- Find the names of all airlines that ever flew more than 1000 flights in one
-- day (i.e., a specific day/month, but not any 24-hour period). Return only
-- the names of the airlines. Do not return any duplicates (i.e., airlines
-- with the exact same name). Name the output column name.

SELECT DISTINCT C.name AS name
FROM FLIGHTS F, CARRIERS C, MONTHS M
WHERE 	F.month_id = M.mid
	AND F.carrier_id = C.cid
GROUP BY F.day_of_month, M.month, C.name
HAVING COUNT(*) > 1000;
-- number of rows: 12
