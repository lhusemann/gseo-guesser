package com.gse23.lhusemann;

import android.util.Log;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    private PropertyChangeSupport support;
    TimerTask timerTask;
    Timer timer;
    int counter;
    boolean rundeVorbei = false;
    boolean zeitAbgelaufen = false;
    private static final String NOGUESS = "Kein Guess abgegeben.";
    Game() {
        this.support = new PropertyChangeSupport(this);
    }
    public void start() {
            this.support.firePropertyChange("start", true, false);
    }
    public void mapClick() {
        if (!rundeVorbei) {
            this.support.firePropertyChange("NewMarkCheck", false, true);
        }
    }
    public void validClick(double longitude, double latitude) {
        if (rundeVorbei) {
            this.support.firePropertyChange("snackbar", "", "Runde vorbei!");
        } else {
            if (Formatierung.isValidLongitude(longitude) && Formatierung.isValidLatitude(latitude)) {
                this.support.firePropertyChange("valid", false, true);
            } else {
                if (!zeitAbgelaufen) {
                    this.support.firePropertyChange("snackbar", "", "Die eingegebenen Werte sind nicht Valide! Der" +
                            " Längengrad darf nur zwischen -180 und +180, und der Breitengrad zwischen -90 und +90 liegen!");
                }
            }
        }
    }
    public void center() {

    }
    public void aufgeben() {

    }
    public void linkClick(String link) {
        if (!link.equals(NOGUESS)) {
            this.support.firePropertyChange("linkClick", false, "#551A8B");
        }
    }
    public void zeitVorbei() {
        zeitAbgelaufen = true;
        this.support.firePropertyChange("valid", false, true);
        rundeVorbei(true);
    }
    public void checkBilder(boolean downloaded, String folder) {
        if (downloaded) {
            this.support.firePropertyChange("isDownloaded", "", folder);
        } else {
            this.support.firePropertyChange("isInternal", "", folder);
        }
    }
    public void forAddBilderWithName(String[] names, String folder, boolean downloaded) {
        boolean noImageException = true;
        for (String name : names) {
            if (Bild.isImage(name)) {
                noImageException = false;
                if (downloaded) {
                    this.support.firePropertyChange("addBildD", "", name);
                } else {
                    this.support.firePropertyChange("addBild", "", name);
                }
            }
        }
        if (noImageException) {
            try {
                throw new NoImagesInAlbumException();
            } catch (NoImagesInAlbumException e) {
                this.support.firePropertyChange("NoImagesInAlbumException", "", folder);
            }
        }
    }
    //Hilfsfunktion, da ich für die Downloadeten Sachen entwas anders agieren muss
    public void forAddBilderLocation(String folderLocation, String folder, boolean downloaded) {
        File albumOrdner = new File(folderLocation);
        File[] albumArray = albumOrdner.listFiles();
        if (albumArray != null) {
            String[] names = new String[albumArray.length];
            for (int i = 0; i < albumArray.length; i++) {
                names[i] = albumArray[i].getName();
            }
            forAddBilderWithName(names, folder, downloaded);
        } else {
            forAddBilderWithName(new String[0], folder, downloaded);
        }

    }
    public void rundeVorbei(boolean vorbei) {
        rundeVorbei = vorbei;
    }

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
