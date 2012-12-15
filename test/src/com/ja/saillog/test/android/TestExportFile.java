package com.ja.saillog.test.android;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import android.os.Environment;
import android.test.AndroidTestCase;

import com.ja.saillog.utilities.ExportFile;

public class TestExportFile extends AndroidTestCase {
    @Override
    protected void setUp() {
        exf = new ExportFile(suffix);
    }

    @Override 
    protected void tearDown() {
        exf = null;
    }

    public void testFileName() {
        int permittedTimeDiff = 2000; // milliseconds.
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();

        Date now = Calendar.getInstance().getTime();

        String name = exf.fileName();

        String patternStr = String.format("%s%sSailLog-export-(\\d{14}).%s",
                                          dir,
                                          File.separator,
                                          suffix);
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(name);

        Assert.assertTrue(String.format("File name %s is not in expected format %s", name, patternStr),
                          matcher.matches());

        String timestampStr = matcher.group(1);
        Date fileTimeStamp = null;
        try {
            fileTimeStamp = new SimpleDateFormat("yyyyMMddHHmmss").parse(timestampStr);
        } catch (ParseException e) {
            Assert.assertTrue(e.toString(), false);
        }

        Assert.assertTrue(String.format("File time stamp %s is off too much. Expected time is %s",
                                        timestampStr,
                                        new SimpleDateFormat("yyyyMMddHHmmss").format(now)),
                                        (fileTimeStamp.getTime() - now.getTime() < permittedTimeDiff));
    }


    public void testFileWritable() {
        File f = null;

        try {
            f = exf.file();
            System.err.println("testFileWritable: name is " + f.getAbsolutePath());

            Assert.assertTrue(f.canWrite());

            FileWriter fw = new FileWriter(f);
            fw.write("abc");
            fw.close();

            // Check that the file is really there and that it contains the
            // expected number of characters.
            File readF = new File(exf.fileName());
            Assert.assertEquals(readF.length(), 3);
        } catch (IOException iox) {
            Assert.assertTrue(String.format("Caught io exception while trying to write to export file %s: %s", 
                                            exf.fileName(), iox.toString()),
                                            false);
        }
        finally {
            if (null != f) {
                f.delete();
            }
        }
    }

    public void testExportDirAvailable() {
        // Too bad it is difficult to test a non-writable MMC directory. 
        // This is a null test now.
        Assert.assertTrue("Export directory is not writable. Please check that USB mass storage mode is not active.",
                          true == exf.isExportDirAvailable());
    }

    private ExportFile exf;
    private static final String suffix = "foo";
}
