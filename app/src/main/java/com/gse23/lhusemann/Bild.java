package com.gse23.lhusemann;
/**
 * Das ist die Klasse Bild in die ich als Datenstruktur verwende.
 */
public class Bild {
    String name;
    String description;
    double longtitude;
    double latitude;
    Bild(String pName, String pDescription, Double pLongtitude, Double pLatitude) {
        name = pName;
        description = pDescription;
        longtitude = pLongtitude;
        latitude = pLatitude;
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
}
