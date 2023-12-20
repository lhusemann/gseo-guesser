package com.gse23.lhusemann;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * Das ist eine Exception die geworfen wird, wenn keine Bilder in Album existieren.
 */
public class NoImagesInAlbumException extends Exception {
    public NoImagesInAlbumException(String folder, Activity activity) {
        Log.i("NoImagesInAlbumException", folder + " hat keine Bilddateien enthalten.");
        activity.finish();
        Toast.makeText(activity, "Keine Bilder im ausgewaehltem Album vorhanden.", Toast.LENGTH_SHORT).show();
    }
}
