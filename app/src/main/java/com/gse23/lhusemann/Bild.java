package com.gse23.lhusemann;

public class Bild {
    String name;
    String description;
    Double longtitude;
    Double latitude;
    Bild(String pName, String pDescription, Double pLongtitude, Double pLatitude) {
        name = pName;
        description = pDescription;
        longtitude = pLongtitude;
        latitude = pLatitude;
    }
    public String getName() {
        return name;
    }
}
