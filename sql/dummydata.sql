
-- Some dummy example data.

BEGIN;
DELETE FROM position;
DELETE FROM trip;

INSERT INTO trip VALUES (1, 'Test trip');

INSERT INTO position (trip_id, pos_time, latitude, longitude, 
       	    	      speed, bearing)
VALUES (1, CURRENT_TIMESTAMP, 60.1, 25.2, 1.1, 20.0);
INSERT INTO position (trip_id, pos_time, latitude, longitude, 
       	    	      speed, bearing)
VALUES (1, CURRENT_TIMESTAMP, 60.1, 25.2, 1.1, 20.0);
INSERT INTO position (trip_id, pos_time, latitude, longitude, 
       	    	      speed, bearing)
VALUES (1, CURRENT_TIMESTAMP, 60.12, 25.19, 1.1, 20.0);

COMMIT;
