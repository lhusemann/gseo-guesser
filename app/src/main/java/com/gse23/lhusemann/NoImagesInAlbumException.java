package com.gse23.lhusemann;

import android.util.Log;

public class NoImagesInAlbumException extends Exception {
    public NoImagesInAlbumException(String folder) {
        Log.i("NoImagesInAlbumException",folder + " hat keine Bilddateien enthalten. Du wirst auf die " +
                "Startansicht zurück geworfen");
    }
}