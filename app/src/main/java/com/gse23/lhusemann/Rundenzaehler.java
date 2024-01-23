package com.gse23.lhusemann;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Rundenzaehler {
    private int runde;
    private int maxRunde;
    private int rundenlaenge;
    private PropertyChangeSupport support;
    public Rundenzaehler(int runde, int maxRunde, int rundenLaenge) {
        this.runde = runde;
        this.maxRunde = maxRunde;
        this.rundenlaenge = rundenLaenge;
        this.support = new PropertyChangeSupport(this);
    }
    public void setRunde(int runde) {
        this.support.firePropertyChange("Runde", this.runde, runde);
        this.runde = runde;
    }
    public void incRunde(int albumSize) {
        if (albumSize > 0) {
            if (runde + 1 <= maxRunde) {
                this.support.firePropertyChange("BildInc", this.runde-1, runde);
                this.support.firePropertyChange("RundenInc", this.runde, runde + 1);
            } else {
                this.support.firePropertyChange("goEndScreen", this.runde, runde + 1);
            }
            this.runde++;
        }
    }
    public int getRunde() {
        return runde;
    }
    public void setMaxRunde(int maxRunde) {
        this.support.firePropertyChange("maxRunde", this.maxRunde, maxRunde);
        this.maxRunde = maxRunde;
    }
    public int getMaxRunde() {
        return maxRunde;
    }
    public void setRundenlaenge(int rundenlaenge) {
        this.support.firePropertyChange("rundenlaenge", this.rundenlaenge, rundenlaenge);
        this.rundenlaenge = rundenlaenge;
    }
    public int getRundenlaenge() {
        return rundenlaenge;
    }


    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
