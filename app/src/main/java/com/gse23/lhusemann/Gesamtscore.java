package com.gse23.lhusemann;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Gesamtscore {
    private int score;
    private PropertyChangeSupport support;
    public Gesamtscore(int score) {
        this.score = score;
        this.support = new PropertyChangeSupport(this);
    }
    public void incGesamtscore(int score) {
        this.support.firePropertyChange("IncGesamtscore", this.score, this.score + score);
        this.score += score;
    }
    public void setGesamtscore(int score) {
        this.support.firePropertyChange("Gesamtscore", this.score, score);
        this.score = score;
    }
    public int getGesamtscore() {
        return score;
    }
    public String getStringGesamtscore() {
        return "" + score;
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
