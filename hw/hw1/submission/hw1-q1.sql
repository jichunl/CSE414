-- CSE 414 	Fall 2018
-- Homework1 - Q1
-- Name: Jichun Li
-- ID:	1531264

.header on
.mode col

-- Create a table Edges(Source, Destination) where both Source and Destination
--	 are integers.

CREATE TABLE Edges (Source int, Destination int);


-- Insert the tuples (10,5), (6,25), (1,3), and (4,4)

INSERT INTO Edges (Source, Destination)
VALUES (10,5), (6,25), (1,3), (4,4);


-- Write a SQL statement that returns all tuples.

SELECT * FROM Edges;


-- Write a SQL statement that returns only column Source for all tuples.

SELECT Source FROM Edges;


-- Write a SQL statement that returns all tuples where Source > Destination.

SELECT * FROM Edges
WHERE Source > Destination;


-- Now insert the tuple ('-1','2000'). Do you get an error? Why?
-- There is no error.
INSERT INTO Edges (Source, Destination)
VALUES ('-1','2000');
