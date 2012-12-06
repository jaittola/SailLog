package com.ja.saillog;

import java.io.IOException;

/**
 * An Interface for implementing mocks of the actual
 * position and event database interface.
 */
public interface TrackDBInterface {
	public void insertPosition(double latitude, double longitude, 
							   double bearing, double speed);
	
	public void insertEvent(int engineStatus, int sailPlan);

    public void exportDbAsKML(ExportFile exportFile) throws IOException;

    public void exportDbAsSQLite(ExportFile exportFile) throws IOException;

    public void close();
}
