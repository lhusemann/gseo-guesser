package com.gse23.lhusemann;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

public class Punkte {
    final double minDistance = 10.0;
    final double maxDistance = 10000.0;
    final int maxPoints = 5000;
    int punkte;
    ArrayList<Integer> punkteListe = new ArrayList<>();
    private PropertyChangeSupport support;
    Punkte(int punkte) {
        this.punkte = punkte;
        this.support = new PropertyChangeSupport(this);
    }
    public double berechne(double distanz) {
        int punkteDavor = this.punkte;
        if (distanz < minDistance) {
            punkte = maxPoints;
        } else if (distanz > maxDistance) {
            punkte = 0;
        } else {
            punkte = (int) ((int) (maxPoints / Math.log(maxDistance / minDistance)) * Math.log(maxDistance / distanz));
        }
        this.support.firePropertyChange("Punkte", punkteDavor, punkte);
        return punkte;
    }
    public int getPunkte() {
        return punkte;
    }
    public String getStringPunkte() {
        return "" + punkte;
    }
    public void reset() {
        this.support.firePropertyChange("Punkte", punkte, 0);
        this.punkte = 0;
    }
    public void safePunkte() {
        punkteListe.add(punkte);
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
