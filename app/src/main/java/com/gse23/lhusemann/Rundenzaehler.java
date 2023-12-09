package com.gse23.lhusemann;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Rundenzaehler {
    private int runde;
    private PropertyChangeSupport support;
    public Rundenzaehler(int runde) {
        this.runde = runde;
        this.support = new PropertyChangeSupport(this);
    }
    public void setRunde(int runde) {
        this.support.firePropertyChange("Runde", this.runde, runde);
        this.runde = runde;
    }
    public int getRunde() {
        return runde;
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
