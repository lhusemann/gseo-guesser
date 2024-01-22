package com.gse23.lhusemann;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * Das ist eine Exception die geworfen wird, wenn Exifdaten in einem Bild fehlen
 */
public class CorruptedExifDataException extends Exception {
    public CorruptedExifDataException() {
    }
}
