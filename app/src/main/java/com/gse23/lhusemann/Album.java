package com.gse23.lhusemann;

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
            this.support.firePropertyChange("logName", "", album.get(index).getName());
            if (album.get(index).getDescription() == null) {
                this.support.firePropertyChange("logNoDescription", "", "-");
            } else {
                this.support.firePropertyChange("logDescription", "", album.get(index).getDescription());
            }
            this.support.firePropertyChange("logLongtitude", "", "" + album.get(index).getLongtitude());
            this.support.firePropertyChange("logLatitude", "", "" + album.get(index).getLatitude());
        }
    }
    public void logExifNoDec(int index) {
        if (album.size() >= index) {
            this.support.firePropertyChange("logName", "", album.get(index).getName());
            this.support.firePropertyChange("logLongtitude", "", "" + album.get(index).getLongtitude());
            this.support.firePropertyChange("logLatitude", "", "" + album.get(index).getLatitude());
        }
    }
    public void getDrawable(int index) {
        if (album.size() >= index) {
        if (downloaded) {
            this.support.firePropertyChange("getDrawableD", "/Album/", "/Album/" + name + "/" + album.get(index).getName());
        } else {
            this.support.firePropertyChange("getDrawableI", "albums/", "albums/" + name + "/" + album.get(index).getName());
        }
        } else {
            this.support.firePropertyChange("ErrorBackMenu", "", "Fehler beim Laden des Bildes");
        }
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
