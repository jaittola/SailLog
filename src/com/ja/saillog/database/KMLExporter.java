package com.ja.saillog.database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.utilities.ExportFile;
import com.ja.saillog.utilities.Propulsion;
import com.ja.saillog.utilities.StaticStrings;

public class KMLExporter {
    public void export(SQLiteDatabase db, ExportFile exportFile)
        throws IOException {

        ZipOutputStream zos = null;
        PrintWriter pw = null;

        if (null == exportFile.compressedFileName()) {
            pw = new PrintWriter(exportFile.file(), "utf-8");
        }
        else {
            zos = new ZipOutputStream(new FileOutputStream(exportFile.compressedFileName()));
            zos.putNextEntry(new ZipEntry(exportFile.uncompressedFileNameNoPath()));
            pw = new PrintWriter(zos);
        }

        // KML output below.
        makeKMLHeading(pw);

        // Grey if there is no sail or engine configuration.
        makeLineStyle(pw, "ff999999", styleUnknown);
        // Engine with red color.
        makeLineStyle(pw, "ffff0000", styleEngine);
        // Sailing with green.
        makeLineStyle(pw, "ff00ff00", styleSailing);
        // Motorsailing with lila.
        makeLineStyle(pw, "ffff00ff", styleMotorSailing);

        // TODO
        // - Trip name
        // - Trip description
        // - Event filtering (no duplicates on the same location).

        writeTrackLine(db, pw);
        writeEventMarkers(db, pw);

        pw.println("</Document>");
        pw.println("</kml>");

        pw.flush();

        if (null != zos) {
            zos.closeEntry();
            pw.close();
            zos.close();
        }
        else {
            pw.close();
        }
    }

    public void writeTrackLine(SQLiteDatabase db, PrintWriter pw) {

        String styleName = styleUnknown;
        String nextStyleName = null;
        double currLat = Double.NaN;
        double currLon = Double.NaN;
        Propulsion propulsion = new Propulsion();

        Cursor positions = null;
        
        try {
            positions = getPositionQuery(db);
            
            if (0 >= positions.getCount()) {
                return;
            }

            styleName = getLineStyle(getInitialPropulsionConfiguration(db));

            // Write out a line that shows the route.

            startLineString(pw, styleName, eventName(propulsion));

            while (true == positions.moveToNext()) {
                currLat = positions.getDouble(1);
                currLon = positions.getDouble(2);
                
                // If there is event information, end the line, swap the style,
                // and begin a new line.
                propulsion = getPropulsion(positions, 6, 5);
                if (null != propulsion) {
                    nextStyleName = getLineStyle(propulsion);
                    if (false == nextStyleName.equals(styleName)) {
                        styleName = nextStyleName;
                        writeCoordinates(pw, currLon, currLat);
                        
                        endLineString(pw);
                        startLineString(pw, styleName,
                                        eventName(propulsion));
                    }
                }

                writeCoordinates(pw, currLon, currLat);
            }

            endLineString(pw);
        } finally {
            positions.close();
        }
    }

    public void writeEventMarkers(SQLiteDatabase db, PrintWriter pw) {
 
        Cursor events = null;
        
        try {
            events = getEventMarkerQuery(db);
            
            while (true == events.moveToNext()) {
                writeEventMarker(pw,
                                 getDate(events, 2),    // timestamp (ms)
                                 getPropulsion(events, 4, 3),
                                 events.getDouble(5),   // latitude
                                 events.getDouble(6));  // longitude
            }
        } finally {
            events.close();
        }
    }

    public void writeEventMarker(PrintWriter pw,
                                  Date timestamp,
                                  Propulsion propulsion,
                                  double latitude,
                                  double longitude) {
        pw.println("<Placemark>");
        pw.println("<name>" + eventName(propulsion) + "</name>");
        pw.println("<description><![CDATA[");
        p(pw, date(timestamp));
        p(pw,
          String.format("%s<br>%s",
                        QuantityFactory.dmsLatitude(latitude).withUnit(),
                        QuantityFactory.dmsLongitude(longitude).withUnit()));
        p(pw, StaticStrings.engine() + ": " +
          (propulsion.getEngine() ?
           StaticStrings.on(): StaticStrings.off()));

        List<Pair<String, String> > sails = propulsion.currentSails();
        for (Pair<String, String> p: sails) {
            p(pw, p.first + ": " + p.second);
        }

        pw.println("]]></description>");
        pw.println("<Point>");
        pw.println("<coordinates>");

        writeCoordinates(pw, longitude, latitude);

        pw.println("</coordinates>");
        pw.println("</Point>");
        pw.println("</Placemark>");
    }

    private void p(PrintWriter pw, String text) {
        pw.println("<p>");
        pw.println(text);
        pw.println("</p>");
    }

    private String date(Date timestamp) {
        return DateFormat.getDateTimeInstance().format(timestamp);
    }

    private String eventName(Propulsion propulsion) {
        return propulsion.generalDescription();
    }

    private void makeKMLHeading(PrintWriter pw) {
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        pw.println("<Document>");
    }

    private void makeLineStyle(PrintWriter pw,
                               String rgb,
                               String styleName) {
        pw.println("<Style id=\"" + styleName + "\">");
        pw.println("<LineStyle><color>" +
                   rgb +
                   "</color><width>4</width></LineStyle>");
        pw.println("</Style>");
    }

    private void startLineString(PrintWriter pw,
                                 String styleName,
                                 String name) {
        pw.println("<Placemark>");
        pw.println("<name>" + name + "</name>");
        pw.println("<styleUrl>#" + styleName + "</styleUrl>");
        pw.println("<LineString>");
        pw.println("<coordinates>");
    }

    private void endLineString(PrintWriter pw) {
        pw.println("</coordinates>");
        pw.println("</LineString>");
        pw.println("</Placemark>");
    }

    private void writeCoordinates(PrintWriter pw,
                                  double lon,
                                  double lat) {
        if (false == Double.isNaN(lon) &&
            false == Double.isNaN(lat)) {
            pw.println(String.format("%f,%f", lon, lat));
        }
    }

    private String getLineStyle(Propulsion config) {
        if (null == config) {
            return styleUnknown;
        }
        
        if (Propulsion.engineOn == config.getEngine()) {
            if (0 == config.getSailPlan()) {
                return styleEngine;
            }
            else {
                return styleMotorSailing;
            }
        }
        else if (0 != config.getSailPlan()) {
            return styleSailing;
        }

        return styleUnknown;
    }

    protected Propulsion getInitialPropulsionConfiguration(SQLiteDatabase db) {
        String [] selectionArgs = {};
        Cursor initialConfig = null;

        try {
            initialConfig = db.rawQuery("SELECT engine, sailplan " +
                                        "FROM event " +
                                        "WHERE event_id = " +
                                        "  (SELECT MAX(event_id) " +
                                        "   FROM event " +
                                        "   WHERE position_id = 0)",
                                        selectionArgs);
            if (true == initialConfig.moveToNext()) {
                return getPropulsion(initialConfig, 1, 0);
            }

            return null;
        }
        finally {
            initialConfig.close();
        }
    }

    public Cursor getPositionQuery(SQLiteDatabase db) {
        String [] selectionArgs = {};
        
        return db.rawQuery("SELECT position_id, " +
                "       latitude, " +
                "       longitude, " +
                "       NULL, " +
                "       NULL, " +
                "       NULL, " +
                "       NULL " +
                "FROM position " +
                "WHERE position_id NOT IN " +
                "    (SELECT DISTINCT(position_id) " +
                "     FROM event " +
                "     WHERE position_id IS NOT NULL) " +
                "UNION ALL " +
                "SELECT p.position_id, " +
                "       p.latitude, " +
                "       p.longitude, " +
                "       e.event_id, " +
                "       strftime('%s', e.event_time), " +
                "       e.engine, " +
                "       e.sailplan " +
                "FROM POSITION p " +
                "JOIN event e ON p.position_id = e.position_id " +
                "WHERE event_id IN (SELECT MAX(event_id) " +
                "                   FROM event " +
                "                   GROUP BY position_id) " +
                "ORDER BY position_id",
                selectionArgs);
    }
    
    public Cursor getEventMarkerQuery(SQLiteDatabase db) {
        String [] selectionArgs = {};
        return db.rawQuery("SELECT e.event_id, " +
                                    "       p.position_id, " +
                                    "       strftime('%s', e.event_time), " +
                                    "       e.engine, " +
                                    "       e.sailplan, " +
                                    "       p.latitude, " +
                                    "       p.longitude " +
                                    "FROM event e " +
                                    "JOIN position p " +
                                    "WHERE e.event_id = (SELECT MAX(event_id) "+
                                    "                    FROM event " +
                                    "                    WHERE position_id= 0) "+
                                    "AND p.position_id = (SELECT " +
                                    "                     MIN(position_id) " +
                                    "                     FROM position) " +
                                    "UNION ALL " +
                                    "SELECT e.event_id, " +
                                    "       p.position_id, " +
                                    "       strftime('%s', e.event_time), " +
                                    "       e.engine, " +
                                    "       e.sailplan, " +
                                    "       p.latitude, " +
                                    "       p.longitude " +
                                    "FROM EVENT e " +
                                    "JOIN position p " +
                                    "ON e.position_id = p.position_id " +
                                    "WHERE e.event_id IN (SELECT MAX(event_id) "+
                                    "                     FROM event " +
                                    "                     GROUP BY position_id) "+
                                    "ORDER BY event_id",
                                    selectionArgs);

        
    }
    
    // This is copypasta and should be moved to some
    // central utility.
    private Date getDate(Cursor c, int column) {
        if (true == c.isNull(column)) {
            return null;
        }

        return new Date(c.getLong(column) * 1000);
    }

    private Propulsion getPropulsion(Cursor c,
                                     int sailPlanColumn,
                                     int engineColumn) {
        if (false == c.isNull(sailPlanColumn) &&
            false == c.isNull(engineColumn)) {
            return new Propulsion(c.getLong(sailPlanColumn),
                                  0 != c.getLong(engineColumn));
        }

        return null;
    }

    private static final String styleUnknown = "unknown";
    private static final String styleEngine = "engine";
    private static final String styleSailing = "sailing";
    private static final String styleMotorSailing = "motorSailing";
}
