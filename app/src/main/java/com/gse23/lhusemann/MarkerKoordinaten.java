package com.gse23.lhusemann;

public class MarkerKoordinaten {
    private double longitude;
    private double latitude;
    private boolean isPlaced = false;
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
    public boolean isPlaced() {
        return isPlaced;
    }
    public void setIsPlaced(boolean isPlaced) {
        this.isPlaced = isPlaced;
    }
    public void setZero() {
        longitude = 0.0;
        latitude = 0.0;
        isPlaced = false;
    }
}
