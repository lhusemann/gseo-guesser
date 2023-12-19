package com.gse23.lhusemann;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class MarkerKoordinaten {
    private double longitude;
    private double latitude;
    private PropertyChangeSupport support;
    public MarkerKoordinaten(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.support = new PropertyChangeSupport(this);
    }
    public void setLongitude(double longitude) {
        this.support.firePropertyChange("Laengengrad", this.longitude, longitude);
        this.longitude = longitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLatitude(double latitude) {
        this.support.firePropertyChange("Breitengrad", this.latitude, latitude);
        this.latitude = latitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
