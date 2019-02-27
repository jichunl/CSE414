DROP TABLE IF EXISTS Reservation;
DROP TABLE IF EXISTS ID;
DROP TABLE IF EXISTS Users;
 
CREATE TABLE Users (username varchar(20) collate Latin1_General_CS_AS primary key, 
		    password varchar(20) collate Latin1_General_CS_AS, 
		    balance int
		   );

CREATE TABLE Reservation (
			username varchar(20) collate Latin1_General_CS_AS,
			reservationId int PRIMARY KEY,
			Day int,
			fid1 int,
			fid2 int,
			pay varchar(5),
			price int,
			FOREIGN KEY (username) REFERENCES Users(username),
			);

CREATE TABLE ID (reservationId int);
INSERT INTO ID values(0);
