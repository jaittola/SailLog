
-- Fetch all trip data from a database

SELECT trip_name, 
       pos_time, 
       latitude, 
       longitude, 
       speed, 
       bearing
FROM trip
INNER JOIN position ON (trip.trip_id = position.trip_id);
