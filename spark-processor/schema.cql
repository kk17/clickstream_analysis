//Create keyspace
CREATE KEYSPACE IF NOT EXISTS ClickstreamKeySpace WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

//Create table
CREATE TABLE ClickstreamKeySpace.UserStatus (userId text, lastSessionId text,  cartAmount double, lastVisitTime timestamp,PRIMARY KEY (userId));


//Select from table
SELECT * FROM ClickstreamKeySpace.UserStatus;


//Truncate table
TRUNCATE TABLE ClickstreamKeySpace.UserStatus;
