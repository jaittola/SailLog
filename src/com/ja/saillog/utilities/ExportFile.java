package com.ja.saillog.utilities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

public class ExportFile {
    public ExportFile(String suffix) {
        this.suffix = suffix;
    }
    
    public String fileName() {        
        Date now = Calendar.getInstance().getTime();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(now);
       
        return String.format("%s%sSailLog-export-%s.%s",
                             Environment.getExternalStorageDirectory().getAbsolutePath(),
                             File.separator,
                             timestamp,
                             suffix);
    }
    
    public File file() throws IOException {
        File f = new File(fileName());
        f.createNewFile();
        return f;
    }
    
    public boolean isExportDirAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    
    private String suffix;
}
