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
    
    public ExportFile(String suffix, String compressedSuffix) {
        this.suffix = suffix;
        this.compressedSuffix = compressedSuffix;
    }
    
    public String fileName() {
        return String.format("%s.%s", fileNameWithPath(), suffix);
    }
        
    public String compressedFileName() {
        if (null == compressedSuffix) {
            return null;
        }
        
        return String.format("%s.%s", fileNameWithPath(), compressedSuffix);
    }
    
    public String uncompressedFileNameNoPath() {
        return String.format("%s.%s", fileNameBody(), suffix);
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

    private String fileNameWithPath() {
        return String.format("%s%s%s", 
                             Environment.getExternalStorageDirectory().getAbsolutePath(),
                             File.separator,
                             fileNameBody());
    }
    
    private String fileNameBody() {
        if (null == fileNameBody) {
            Date now = Calendar.getInstance().getTime();
       
            fileNameBody = 
                    "SailLog-export-" + 
                    new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(now);
        }
        
        return fileNameBody;
    }

    
    private String fileNameBody = null;
    private String suffix = null;
    private String compressedSuffix = null;
}
