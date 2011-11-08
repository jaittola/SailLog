
-- Tables for SailLog's database
--
-- This will probably not be needed for real. However, now it acts as
-- a plan, and documents what is in the db.

PRAGMA auto_vacuum = INCREMENTAL;

DROP TABLE IF EXISTS trip;
CREATE TABLE trip (
   trip_id INTEGER PRIMARY KEY AUTOINCREMENT,
   trip_name VARCHAR(180) NOT NULL
);

-- This is not used at the moment.
-- Maybe later
DROP TABLE IF EXISTS leg;
-- CREATE TABLE leg (
--   leg_id INTEGER PRIMARY KEY AUTOINCREMENT,
--   trip_id INTEGER NOT NULL,
--   leg_name VARCHAR(180) NOT NULL,
--   start_pos VARCHAR(180),
--   end_pos VARCHAR(180)
-- );

DROP TABLE IF EXISTS position;
CREATE TABLE position (
   position_id INTEGER PRIMARY KEY AUTOINCREMENT,
--   leg_id INTEGER NOT NULL,
   trip_id INTEGER NOT NULL,
   pos_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   latitude DOUBLE NOT NULL,
   longitude DOUBLE NOT NULL,
   speed DOUBLE NOT NULL,
   bearing DOUBLE NOT NULL
);

   
