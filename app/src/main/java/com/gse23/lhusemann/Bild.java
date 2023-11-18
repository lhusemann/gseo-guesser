package com.gse23.lhusemann;
/**
 * Das ist die Klasse Bild in die ich als Datenstruktur verwende.
 */
public class Bild {
    String name;
    String description;
    double longtitude;
    double latitude;
    //Konstruktor von Bild
    Bild(String pName, String pDescription, Double pLongtitude, Double pLatitude) {
        name = pName;
        description = pDescription;
        longtitude = pLongtitude;
        latitude = pLatitude;
    }
    //Methode um Namen zu bekommen
    public String getName() {
        return name;
    }
    //Methode um Bescheibung zu bekommen
    public String getDescription() {
        return description;
    }
    //Methode um Laengengrad zu bekommen
    public Double getLongtitude() {
        return longtitude;
    }
    //Methode um Breitengrad zu bekommen
    public Double getLatitude() {
        return latitude;
    }
}
