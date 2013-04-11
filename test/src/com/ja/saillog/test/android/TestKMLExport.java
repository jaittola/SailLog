package com.ja.saillog.test.android;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import junit.framework.Assert;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ja.saillog.database.KMLExporter;
import com.ja.saillog.database.TrackDB;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.utilities.ExportFile;
import com.ja.saillog.utilities.Propulsion;

public class TestKMLExport extends TestDbBase {
    @Override
    protected void setUp() throws Exception {

        super.setUp();

        // Eventually this configuration should be stored in the
        // database. But as we do not yet have a configurable
        // sail plan, we can have it hard-coded like this.

        Propulsion.addSail("Main");
        Propulsion.addSail("Jib");
        Propulsion.addSail("Spinnaker");

        db = new TrackDB(mContext, "SLDB-test-export.db");
    }

    @Override
    protected void tearDown() throws Exception {
        Propulsion.clearSails();

        super.tearDown();
    }

    /**
     * Stores a result row of the track query.
     */
    private class TrackDataItem {
        public TrackDataItem(Double lat,
                             Double lon,
                             Integer sails,
                             Integer engine) {
            this.lat = lat;
            this.lon = lon;
            this.sails = sails;
            this.engine = engine;
        }

        public Double lat;
        public Double lon;
        public Integer sails;
        public Integer engine;
    }

    // Data that has position and event data from the beginning.
    // This variable is here as class data because it gets used
    // in two test cases.
    private TrackDataItem[] trackDataItems1 = {
        new TrackDataItem(60.0, 25.0, 0, 1),
        new TrackDataItem(60.1, 25.1, null, null),
        new TrackDataItem(60.2, 25.2, 1, 0),
        new TrackDataItem(60.3, 25.3, null, null),
        new TrackDataItem(60.4, 25.4, 0, 0),
    };

    public void testTrackDataString1() {
 
        String expectedTrack = makeXML(new String[] {
                "<Placemark>",
                "<name>motoring</name>",
                "<styleUrl>#engine</styleUrl>",
                "<LineString>",
                "<coordinates>",
                "25.000000,60.000000",
                "25.100000,60.100000",
                "25.200000,60.200000",
                "</coordinates>",
                "</LineString>",
                "</Placemark>",

                "<Placemark>",
                "<name>sailing</name>",
                "<styleUrl>#sailing</styleUrl>",
                "<LineString>",
                "<coordinates>",
                "25.200000,60.200000",
                "25.300000,60.300000",
                "25.400000,60.400000",
                "</coordinates>",
                "</LineString>",
                "</Placemark>",
                "<Placemark>",
                "<name>drifting</name>",
                "<styleUrl>#unknown</styleUrl>",
                "<LineString>",
                "<coordinates>",
                "25.400000,60.400000",
                "</coordinates>",
                "</LineString>",
                "</Placemark>",
        });
        
        verifyTrackString(trackDataItems1, expectedTrack);
    }

    public void testTrackDataString2() {
        // This data begins with location information and the events
        // come later.
        TrackDataItem[] trackDataItems2 = {
                new TrackDataItem(60.0, 25.0, null, null),
                new TrackDataItem(60.1, 25.1, null, null),
                new TrackDataItem(60.2, 25.2, 1, 0),
                new TrackDataItem(60.3, 25.3, null, null),
                new TrackDataItem(60.4, 25.4, 0, 0),
        };

        // This stuff could come from a file, but it is a bit
        // challenging to transport that data to the device (or
        // emulator) for testing.
        String expectedTrack = makeXML(new String[] {
                "<Placemark>",
                "<name>drifting</name>",
                "<styleUrl>#unknown</styleUrl>",
                "<LineString>",
                "<coordinates>",
                "25.000000,60.000000",
                "25.100000,60.100000",
                "25.200000,60.200000",
                "</coordinates>",
                "</LineString>",
                "</Placemark>",
                "<Placemark>",
                "<name>sailing</name>",
                "<styleUrl>#sailing</styleUrl>",
                "<LineString>",
                "<coordinates>",
                "25.200000,60.200000",
                "25.300000,60.300000",
                "25.400000,60.400000",
                "</coordinates>",
                "</LineString>",
                "</Placemark>",
                "<Placemark>",
                "<name>drifting</name>",
                "<styleUrl>#unknown</styleUrl>",
                "<LineString>",
                "<coordinates>",
                "25.400000,60.400000",
                "</coordinates>",
                "</LineString>",
                "</Placemark>",
            });

        verifyTrackString(trackDataItems2, expectedTrack);
    }

    public void testTrackDataString3() {

        // Here we have events first and the location information
        // begins later.
        TrackDataItem[] trackDataItems3 = new TrackDataItem[] {
            new TrackDataItem(null, null, 0, 1),
            new TrackDataItem(null, null, 1, 1),
            new TrackDataItem(null, null, 1, 0),
            new TrackDataItem(60.5, 25.5, null, null),
            new TrackDataItem(60.6, 25.6, 0, 0),
        };

        String expectedTrack = makeXML(new String[] {
                "<Placemark>",
                "<name>sailing</name>",
                "<styleUrl>#sailing</styleUrl>",
                "<LineString>",
                "<coordinates>",
                "25.500000,60.500000",
                "25.600000,60.600000",
                "</coordinates>",
                "</LineString>",
                "</Placemark>",
                "<Placemark>",
                "<name>drifting</name>",
                "<styleUrl>#unknown</styleUrl>",
                "<LineString>",
                "<coordinates>",
                "25.600000,60.600000",
                "</coordinates>",
                "</LineString>",
                "</Placemark>",
        });

        verifyTrackString(trackDataItems3, expectedTrack);
    }

    private void verifyTrackString(TrackDataItem[] items, String expectedTrack) {
        insertTrackAndEvents(items);

        StringWriter sw =  new StringWriter();
        new KMLExporter().writeTrackLine(db.getReadableDatabase(),
                                         new PrintWriter(sw));

        Assert.assertEquals(expectedTrack, sw.toString());
    }

    
    // This is  crappy test case. We insert some data and verify that the
    // result document is parseable (i.e., that it contains valid XML).
    public void testExportGeneratedData() {
        insertTrackAndEvents(trackDataItems1);

        ExportFile ef = new ExportFile("kml");

        try {
            new KMLExporter().export(db.getReadableDatabase(), ef);

            // Verify that the file is ok.
            Document kml = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(ef.file());
        } catch (IOException ex) {
            Assert.fail("Caught IOException: " + ex.toString());
        } catch (ParserConfigurationException pce) {
            Assert.fail("XML parsing fail: " + pce.toString());
        } catch (SAXException sex) {
            Assert.fail("SAX XML parser exception: " + sex.toString());
        }

        // Clean up: wipe out the export file.
        try {
            ef.file().delete();
        }
        catch (Exception ex) { }
    }

    private String makeXML(String[] lines) {
        StringWriter wr = new StringWriter();
        PrintWriter pw = new PrintWriter(wr);
        for (String line: lines) {
            pw.println(line);
        };

        return wr.toString();
    }

    protected void insertTrackAndEvents(TrackDataItem[] items) {

        for (TrackDataItem tdi: items) {
            // Note that this data is completely nonsense. The distance is off
            // and so on.
            // This null checking is also a bit stupid.
            if (null != tdi.lat &&
                null != tdi.lon) {
                db.insertPosition(tdi.lat.doubleValue(),
                                  tdi.lon.doubleValue(),
                                  30,
                                  QuantityFactory.knots(3),
                                  QuantityFactory.meters(200),
                                  1.0);
            }
            if (null != tdi.sails && null != tdi.engine) {
                db.insertEvent(new Propulsion(tdi.sails.longValue(),
                                              0 != tdi.engine));
            }
        }
    }

    protected SQLiteDatabase getWritableDatabase() {
        return db.getWritableDatabase();
    }

    protected TrackDB db = null;
}
