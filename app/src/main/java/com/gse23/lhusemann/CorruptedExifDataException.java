package com.gse23.lhusemann;

import android.util.Log;

public class CorruptedExifDataException extends Exception {
    public CorruptedExifDataException(String exifTag, String filepath) {
        Log.i("CorruptedExifDataException",exifTag + " konnte in " + filepath + " nicht gefunden werden");
    }
}
