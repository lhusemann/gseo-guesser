package com.gse23.lhusemann;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * Das ist eine Exception die geworfen wird, wenn Exifdaten in einem Bild fehlen
 */
public class CorruptedExifDataException extends Exception {
    public CorruptedExifDataException(String exifTag, String filepath, Activity activity) {
        Log.i("CorruptedExifDataException", exifTag + " konnte in " + filepath + " nicht gefunden werden.");
        activity.finish();
        Toast.makeText(activity, "Eine und mehrere Dateien haben keine Standortdaten.", Toast.LENGTH_SHORT).show();
    }
}
