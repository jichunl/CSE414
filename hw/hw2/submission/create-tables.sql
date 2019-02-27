-- CSE 414 	Autumn 2018
-- Homework2 - create-tables
-- Name: Jichun Li
-- ID:	1531264

.header ON
.mode csv
PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS CARRIERS;
DROP TABLE IF EXISTS MONTHS;
DROP TABLE IF EXISTS WEEKDAYS;
DROP TABLE IF EXISTS FLIGHTS;

-- Create table for Carriers

CREATE TABLE CARRIERS (
	cid varchar(7) PRIMARY KEY,
	name varchar(83)
);


-- Import carrier database

.import carriers.csv CARRIERS


-- Create table for Months

CREATE TABLE MONTHS (
	mid integer PRIMARY KEY, 
	month varchar(9)
);


-- Import months database

.import months.csv MONTHS

-- Create table for Weekdays

CREATE TABLE WEEKDAYS (
	did integer PRIMARY KEY,
	day_of_week varchar(9)
);


-- Import weekday database

.import weekdays.csv WEEKDAYS 


-- Create table for Flights

CREATE TABLE FLIGHTS (
	fid integer PRIMARY KEY,		
	month_id integer,
	day_of_month integer,
	day_of_week_id integer,
	carrier_id varchar(7),
	flight_num integer,
	origin_city varchar(34),
	origin_state varchar(47),
	dest_city varchar(34),
	dest_state varchar(46),
	departure_delay integer,
	taxi_out integer,
	arrival_delay integer,
	canceled integer,
	actual_time integer,
	distance integer,
	capacity integer,
	price integer,
	FOREIGN KEY (carrier_id)
		REFERENCES CARRIERS(cid),
	FOREIGN KEY (month_id)
		REFERENCES MONTHS(mid),
	FOREIGN KEY (day_of_week_id)
		REFERENCES WEEKDAYS(did)
);


-- Import flights database

.import flights-small.csv FLIGHTS

