-- CSE 414 	Autumn 2018
-- Homework3 - Q5
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- List all cities that cannot be reached from Seattle through a direct flight
-- nor with one stop (i.e., with any two flights that go through an intermediate
-- city). Warning: this query might take a while to execute. We will learn about
-- how to speed this up in lecture. (15 points)

-- Name the output column city.

-- (Hint: Do not forget to consider all cities that appear in a flight as an
-- origin_city) (You can assume all cities to be the collection of all
-- origin_city or all dest_city)
SELECT DISTINCT F.dest_city AS city
FROM Flights AS F 
WHERE 	F.dest_city NOT IN (
		SELECT  DISTINCT F_trans2.dest_city AS trans_city
		FROM    Flights AS F_trans1, Flights AS F_trans2
		WHERE   F_trans1.dest_city = F_trans2.origin_city AND
        		F_trans1.origin_city = '"Seattle WA"' 
		) AND 
	F.dest_city NOT IN (
		SELECT DISTINCT F_dir.dest_city AS dir_dest_city
		FROM Flights AS F_dir 
		WHERE F_dir.origin_city = '"Seattle WA"'
		);

-- Number of rows: 3 
-- Time: 41 seconds
-- First 20 rows:
-- "Devils Lake ND"
-- "Hattiesburg/Laurel MS"
-- "St. Augustine FL"
