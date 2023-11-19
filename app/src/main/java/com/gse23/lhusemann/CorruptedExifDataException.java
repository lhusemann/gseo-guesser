package com.gse23.lhusemann;

import android.util.Log;
/**
 * Das ist eine Exception die geworfen wird, wenn Exifdaten in einem Bild fehlen
 */
public class CorruptedExifDataException extends Exception {
    public CorruptedExifDataException(String exifTag, String filepath) {
        Log.i("CorruptedExifDataException", exifTag + " konnte in " + filepath + " nicht gefunden werden."
        + " Kehre auf Startansicht zurück...");
    }
}
