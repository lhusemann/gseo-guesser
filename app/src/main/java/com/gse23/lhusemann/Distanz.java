package com.gse23.lhusemann;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Distanz {
    double distance;
    private PropertyChangeSupport support;
    public Distanz(double distance) {
        this.distance = distance;
        this.support = new PropertyChangeSupport(this);
    }
    public double getDistance() {
        return distance;
    }
    //berechnet Distanz
    public void calcDistance(double latOne, double latTwo, double lonOne, double lonTwo) {
        double distanzDavor = this.distance;
        double deltaLat = getRadians(latTwo - latOne);
        double deltaLon = getRadians(lonTwo - lonOne);
        double haversine = havHelper(deltaLat) + Math.cos(getRadians(latOne)) * Math.cos(getRadians(latTwo))
                * havHelper(deltaLon);
        final double radErde = 6371008.7;
        distance = 2.0 * radErde * Math.asin(Math.sqrt(haversine));
        String wertInKModerM = Formatierung.getUnit(distance);
        this.support.firePropertyChange("DistanzCalc", Double.toString(distanzDavor), wertInKModerM);
    }
    public void resetDistance() {
        double distanzDavor = this.distance;
        distance = 0.0;
        this.support.firePropertyChange("resetDistanz", distanzDavor, distance);
    }
    //rechnet von degree in radians um
    private Double getRadians(Double degree) {
        final double hundertAchtzig = 180.0;
        return degree * Math.PI / hundertAchtzig;
    }
    private Double havHelper(Double theta) {
        return Math.sin(theta / 2) * Math.sin(theta / 2);
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
