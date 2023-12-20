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
    Bild(String name, String description, String longtitude, String latitude) {
        this.name = name;
        this.description = description;
        this.longtitude = makeValue(longtitude);
        this.latitude = makeValue(latitude);
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
    //macht die String-Exifdaten zu dezimalen Graden
    private double makeValue(String tag) {
        String[] dms = tag.split(komma);
        final int dmsNumber = 3;
        if (dms.length == dmsNumber) {
            final double sechsig = 60.0;
            final double sechsigDurchSechsig = 3600.0;
            double degreeNumber = helperMakeValue(dms[0]);
            double minuteNumber = helperMakeValue(dms[1]) / sechsig;
            String[] sec = dms[2].split("/");
            double secondNumber = helperMakeValue(sec[0]) / helperMakeValue(sec[1]) / sechsigDurchSechsig;
            return degreeNumber + minuteNumber + secondNumber;
        } else {
            return 0;
        }
    }
    //macht die String zu verwendbaren Double
    private double helperMakeValue(String tag) {
        StringBuilder valueAsString = new StringBuilder();
        for (int i = 0; i < tag.length() && Character.isDigit(tag.charAt(i)); i++) {
            valueAsString.append(tag.charAt(i));
        }
        if (valueAsString.length() == 0) {
            return 0;
        }
        return Integer.parseInt(valueAsString.toString());
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
