-- CSE 414 	Autumn 2018
-- Homework2 - Q3
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

--  Find the day of the week with the longest average arrival delay. Return the
-- name of the day and the average delay. Name the output columns day_of_week
-- and delay, in that order. (Hint: consider using LIMIT. Look up what it does!)

SELECT 	W.day_of_week AS day_of_week, 
	AVG(F.arrival_delay) as delay
FROM 	FLIGHTS AS F,
	WEEKDAYS AS W
WHERE 	W.did = F.day_of_week_id
GROUP BY W.did
ORDER BY delay DESC
LIMIT 1;
-- number of rows: 1
