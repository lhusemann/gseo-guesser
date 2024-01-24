package com.gse23.lhusemann;

/**
 * Das ist die Klasse Bild in die ich als Datenstruktur verwende.
 */
public class Bild {
    String name;
    String description;
    double longtitude;
    double latitude;

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

    //prüft ob es sich um eine Bild-Datei handelt
    public static boolean isImage(String name) {
        return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
    }
    //das hier in album rein machen und dann mit index von bild als parameter drawable zurück geben
}
