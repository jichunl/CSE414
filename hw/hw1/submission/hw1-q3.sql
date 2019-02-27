-- CSE 414 	Fall 2018
-- Homework1 - Q3
-- Name: Jichun Li
-- ID:	1531264

.header on
.mode col

-- Insert at least five tuples using the SQL INSERT command five (or more) times.
-- You should insert at least one restaurant you liked, at least one restaurant
-- you did not like, and at least one restaurant where you leave the “I like” 
-- field NULL.

INSERT INTO MyRestaurants
VALUES ("Ding Tai Fung","Taiwanese",20,"2018-09-22",1);

INSERT INTO MyRestaurants
VALUES ("Maneki","Japanese",35,"2018-09-11",1);

INSERT INTO MyRestaurants
VALUES ("Sizzling Pot King","Chinese",35,"2018-09-29",1);

INSERT INTO MyRestaurants
VALUES ("Local Point",NULL,5,"2018-09-28",NULL);


INSERT INTO MyRestaurants
VALUES ("Sizzle & Crunch","Vietnamese",8,"2018-05-14",NULL);

INSERT INTO MyRestaurants
VALUES ("Isla Bonita","Mexican",120,"2017-03-18",0);
