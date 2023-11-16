package com.gse23.lhusemann;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import java.io.InputStream;
import java.util.ArrayList;
import android.os.Bundle;
import android.util.Log;

/**
 * Das ist die SpielansichtActivity, die zuerst ausgeführt wird, wenn diese Activity gestartet wird.
 */
public class SpielansichtActivity extends AppCompatActivity {
    String folder = getIntent().getStringExtra("ausgewaehltesAlbum");
    int anzahlBilder = getIntent().getIntExtra("anzahlBilder", 0);
    String album = "albums";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);
        Log.i("test", folder);
        //finish();
    }
    private Bild[] makeImageArray(String folder) {
        Bild[] bilderArray = new Bild[anzahlBilder];
        String[] paths = this.getPath(folder);
        for (int i = 0; i<bilderArray.length;i++) {
            Bild aktuellesBild = new Bild("name", getExifdata(paths[i], ExifInterface.TAG_IMAGE_DESCRIPTION),
                    makeValue(getExifdata(paths[i], ExifInterface.TAG_GPS_LONGITUDE), paths[i]),
                    makeValue(getExifdata(paths[i], ExifInterface.TAG_GPS_LATITUDE), paths[i]));
        }
        return bilderArray;
    }
    private double makeValue (String tag, String path) {
        StringBuilder valueAsString = new StringBuilder("");
        for (int i = 0; Character.isDigit(tag.charAt(i)); i++) {
            valueAsString.append(tag.charAt(i));
        }
        if (valueAsString.equals("")) {
            //Log.i("CorruptedExifDataException", "Die Datei im Verzeichnes" + path + " hat unvollständige Daten.");
            //finish();
            return 0;
        }
        return Integer.parseInt(valueAsString.toString());
    }
    //gibt Path aller Bilder in einem Array wieder
    private String[] getPath(String folder) {
        String[] paths = new String[anzahlBilder];
        int counter = 0;
        String vorPath = album + "/" + folder;
        for (String file : getFolderInhalt(vorPath)) {
            if (this.isImage(file)) {
                paths[counter] = vorPath + "/" + file;
                counter++;
            }
        }
        return paths;
    }
    //gibt Inhalt eines Ordner wieder ohne Fehlermeldung
    private String[] getFolderInhalt(String folder) {
        try {
            return getAssets().list(folder);
        } catch (IOException e) {   //NoImagesInAlbumException
            return new String[0];
        }
    }
    //prüft ob es sich um eine Bild-Datei handelt
    private boolean isImage(String name) {
        int laenge = name.length();
        final int three = 3;
        final int four = 4;
        final int five = 5;
        if (name.charAt(laenge - 1) == 'g') {
            if (name.charAt(laenge - 2) == 'n') {
                if (name.charAt(laenge - three) == 'p') {
                    if (name.charAt(laenge - four) == '.') {
                        return true;
                    }
                }
            }
            if (name.charAt(laenge - 2) == 'p') {
                if (name.charAt(laenge - three) == 'j') {
                    if (name.charAt(laenge - four) == '.') {
                        return true;
                    }
                }
            }
            if (name.charAt(laenge - 2) == 'e') {
                if (name.charAt(laenge - three) == 'p') {
                    if (name.charAt(laenge - four) == 'j') {
                        return name.charAt(laenge - five) == '.';
                    }
                }
            }
        }
        return false;
    }
    private String getExifdata(String filepath, String exifTag) {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            if (exifInterface.getAttribute(exifTag) == null) {
                return "nicht vorhanden";
            } else {
                return exifInterface.getAttribute(exifTag);
            }
        } catch (IOException e) { //CorruptedExifDataException
            return "liegt nicht vor";
        }
    }
}

