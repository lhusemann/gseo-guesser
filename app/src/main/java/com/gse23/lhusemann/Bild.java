package com.gse23.lhusemann;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

/**
 * Das ist die Klasse Bild in die ich als Datenstruktur verwende.
 */
public class Bild {
    String name;
    String description;
    double longtitude;
    double latitude;
    String komma = ",";
    Bild(String name, String description, double longtitude, double latitude) {
        this.name = name;
        this.description = description;
        this.longtitude = longtitude;
        this.latitude = latitude;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public Double getLongtitude() {
        return longtitude;
    }
    public Double getLatitude() {
        return latitude;
    }

    public void logName() {
        Log.i("Bildname", name);
    }
    public void logDescription() {
        Log.i("Beschreibung", "" + description);
    }
    public void logLongtitude() {
        Log.i("Laengengrad", "" + longtitude);
    }
    public void logLatitude() {
        Log.i("Breitengrad", "" + latitude);
    }
    //prüft ob es sich um eine Bild-Datei handelt
    public static boolean isImage(String name) {
        return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
    }
    //macht ein File zu Drawable
    public Drawable getDrawable(String album, Activity activity, boolean downloaded) {
        if (downloaded) {
            String filepath = activity.getFilesDir() + "/Album/" + album + "/" + name;
            return Drawable.createFromPath(filepath);
        } else {
            String filepath = "albums/" + album + "/" + name;
            try (InputStream in = activity.getAssets().open(filepath)) {
                return Drawable.createFromStream(in, "src");
            } catch (IOException e) {
                return null;
            }
        }
    }
}
