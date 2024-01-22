package com.gse23.lhusemann;

import android.util.Log;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;

public class Album {
    ArrayList<Bild> album = new ArrayList<>();
    String name;
    boolean downloaded = false;
    private PropertyChangeSupport support;
    public Album(String name) {
        this.name = name;
        this.support = new PropertyChangeSupport(this);
    }
    public void setName(String name) {
        this.support.firePropertyChange("name", this.name, name);
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
    public boolean isDownloaded() {
        return downloaded;
    }
    public int getSize() {
        return album.size();
    }
    public void addBild(Bild neu) {
        album.add(neu);
        this.support.firePropertyChange("albumErweitert", null, neu);
    }
    public Bild getBild(int index) {
        if (album.size() >= index) {
            return album.get(index);
        } else {
            return null;
        }
    }
    public void mix() {
        ArrayList<Bild> alt = new ArrayList<>(album);
        Collections.shuffle(album);
        this.support.firePropertyChange("mix", alt, album);
    }
    public void logAll() {
        for (int i = 0; i < album.size(); i++) {
            this.logExif(i);
        }
    }
    public void reset() {
        this.support.firePropertyChange("nameReset", this.name, "");
        name = "";
        ArrayList<Bild> alt = new ArrayList<>(album);
        album.clear();
        this.support.firePropertyChange("albumReset", alt, album);
        this.support.firePropertyChange("downloadedReset", downloaded, false);
        this.downloaded = false;
    }
    public void logExif(int index) {
        if (album.size() >= index) {
            album.get(index).logName();
            album.get(index).logDescription();
            album.get(index).logLongtitude();
            album.get(index).logLatitude();
        }
    }
    public void logExifNoDec(int index) {
        if (album.size() >= index) {
            album.get(index).logName();
            album.get(index).logLongtitude();
            album.get(index).logLatitude();
        }
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
