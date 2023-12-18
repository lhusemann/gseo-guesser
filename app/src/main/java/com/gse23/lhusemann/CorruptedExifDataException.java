package com.gse23.lhusemann;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
/**
 * Das ist eine Exception die geworfen wird, wenn Exifdaten in einem Bild fehlen
 */
public class CorruptedExifDataException extends Exception {
    public CorruptedExifDataException(String exifTag, String filepath, Activity activity) {
        Log.i("CorruptedExifDataException", exifTag + " konnte in " + filepath + " nicht gefunden werden.");
        AlertDialog.Builder error = new AlertDialog.Builder(activity);
        error.setMessage("Eine und mehrere Dateien haben keine Standortdaten. Kehre auf Startansicht zurück.")
                .setPositiveButton("ok", (dialog, id) -> {
                    activity.finish();
                })
                .setTitle("Hinweis");
        AlertDialog alert = error.create();
        alert.show();
    }
}
