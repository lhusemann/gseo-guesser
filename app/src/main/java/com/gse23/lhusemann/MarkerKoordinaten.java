package com.gse23.lhusemann;

public class MarkerKoordinaten {
    private double longitude;
    private double latitude;
    private boolean isSet = false;
    public MarkerKoordinaten(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public boolean isSet() {
        return isSet;
    }
    public void setIsSet(boolean isSet) {
        this.isSet = isSet;
    }
    public void setZero() {
        longitude = 0.0;
        latitude = 0.0;
    }
}
