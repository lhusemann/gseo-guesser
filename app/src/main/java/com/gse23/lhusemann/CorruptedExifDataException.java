package com.gse23.lhusemann;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * Das ist eine Exception die geworfen wird, wenn Exifdaten in einem Bild fehlen
 */
public class CorruptedExifDataException extends Exception {
    public CorruptedExifDataException(String fehlendesExif, String filepath, Activity activity) {
        Log.i("CorruptedExifDataException", fehlendesExif + " konnte in " + filepath + " nicht gefunden werden.");
        activity.finish();
        Toast.makeText(activity, fehlendesExif + " fehlt in einer oder mehreren Dateien!", Toast.LENGTH_SHORT).show();
    }
}
