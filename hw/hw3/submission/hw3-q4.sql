-- CSE 414 	Autumn 2018
-- Homework3 - Q4
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode col

-- List all cities that cannot be reached from Seattle though a direct flight
-- but can be reached with one stop (i.e., with any two flights that go
-- through an intermediate city). Do not include Seattle as one of these
-- destinations (even though you could get back with two flights). (15 points)

-- Name the output column city.

-- [Output relation cardinality: 256]


SELECT 	DISTINCT F_trans2.dest_city AS city
FROM	Flights AS F_trans1, Flights AS F_trans2
WHERE	F_trans1.dest_city = F_trans2.origin_city AND
	F_trans1.origin_city = '"Seattle WA"' AND
	F_trans2.dest_city != '"Seattle WA"' AND
	F_trans2.dest_city NOT IN (
		SELECT DISTINCT F_dir.dest_city
		FROM Flights AS F_dir
		WHERE F_dir.origin_city = '"Seattle WA"'
	);
	


-- Number of rows: 256 
-- Time: 20 seconds
-- First 20 rows:
-- "Wichita Falls TX"
-- "Manchester NH"
-- "Ponce PR"
-- "Knoxville TN"
-- "Kinston NC"
-- "Dickinson ND"
-- "Eugene OR"
-- "Worcester MA"
-- "Sioux City IA"
-- "Charlottesville VA"
-- "Saginaw/Bay City/Midland MI"
-- "Billings MT"
-- "Hays KS"
-- "Pocatello ID"
-- "Fayetteville NC"
-- "Muskegon MI"
-- "Gainesville FL"
-- "College Station/Bryan TX"
-- "Pellston MI"
-- "Bismarck/Mandan ND"
