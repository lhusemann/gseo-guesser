package com.gse23.lhusemann;

import android.util.Log;
/**
 * Das ist eine Exception die geworfen wird, wenn keine Bilder in Album existieren.
 */
public class NoImagesInAlbumException extends Exception {
    public NoImagesInAlbumException(String folder) {
        Log.i("NoImagesInAlbumException", folder + " hat keine Bilddateien enthalten.");
    }
}
