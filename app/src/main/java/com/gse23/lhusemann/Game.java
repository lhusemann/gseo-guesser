package com.gse23.lhusemann;

import android.util.Log;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    private PropertyChangeSupport support;
    private Album album;
    private MarkerKoordinaten markerKoordinaten;
    private Rundenzaehler rundenzaehler;
    private Gesamtscore gesamtscore;
    private Punkte punkte;
    private Timer timer;
    private TimerTask timerTask;
    private AlbenListe albenListe;
    private Link link;

    boolean skipMix = false;
    boolean rundeVorbei = false;
    private static final int SEKUNDE = 1000;
    int counter;
    private static final String KOMMA = ",";
    Game() {
        support = new PropertyChangeSupport(this);
        album = new Album();
        markerKoordinaten = new MarkerKoordinaten(0.0,0.0, false);
        rundenzaehler = new Rundenzaehler();
        gesamtscore = new Gesamtscore();
        punkte = new Punkte();
        link = new Link();
    }
    public void start() {
        if (!skipMix) {
            album.mix();
            skipMix = false;
        }
        support.firePropertyChange("Runde", "", rundenzaehler.getRunde());
        support.firePropertyChange("Bild", null, album.getBild(rundenzaehler.getRunde() - 1));
        support.firePropertyChange("Gesamtscore", null, 0);
        support.firePropertyChange("Punkte", null, 0);
        link.reset();
        support.firePropertyChange("Link", null, link);
        startTimer();
    }
    public void nextRound() {
        stopTimer();
        gesamtscore.incGesamtscore(punkte.getCurrentPunkteNum());
        support.firePropertyChange("Gesamtscore", gesamtscore.getGesamtscoreNum() - punkte.getCurrentPunkteNum(), gesamtscore.getGesamtscoreNum());

        punkte.safeCurrentPunkte();

        support.firePropertyChange("Punkte", punkte.getCurrentPunkteNum(), 0);
        punkte.resetCurrent();

        link.reset();
        support.firePropertyChange("Link", null, link);

        // prüft ob Runde noch nicht vorbei
        if (rundenzaehler.incRoundIfAble()) {
            startTimer();
            rundeVorbei = false;
            markerKoordinaten.setIsPlaced(false);
            support.firePropertyChange("markerDisappear", true, false);
            support.firePropertyChange("Beschreibung", album.getBild(rundenzaehler.getRunde() - 2).getDescription(), "");
            support.firePropertyChange("Runde", rundenzaehler.getRunde() - 1, rundenzaehler.getRunde());
            support.firePropertyChange("Bild", album.getBild(rundenzaehler.getRunde() - 2), album.getBild(rundenzaehler.getRunde() - 1));
        } else  {
            support.firePropertyChange("EndGame", true, false);
        }
    }
    public void aufgeben() {
        stopTimer();
        rundenzaehler.reset();
        markerKoordinaten.setZero();
        support.firePropertyChange("Gesamtscore", gesamtscore.getGesamtscoreNum(), 0);
        gesamtscore.reset();
        support.firePropertyChange("Punkte", punkte.getCurrentPunkteNum(), 0);
        punkte.resetAll();
        album.reset();
        album.getSize();
    }
    public void eingabeValidieren() {
        if (rundeVorbei) {
            this.support.firePropertyChange("Alert", "", "Runde vorbei!");
        } else {
            if (markerKoordinaten.isPlaced()) {
                stopTimer();
                support.firePropertyChange("Beschreibung", "", album.getBild(rundenzaehler.getRunde() - 1).getDescription());

                this.support.firePropertyChange("desiredMarker", null, new MarkerKoordinaten(album
                        .getBild(rundenzaehler.getRunde() - 1).getLongtitude(), album.getBild(rundenzaehler
                        .getRunde() - 1).getLatitude(), true));
                double distance = Distanz.calcDistanceNum(markerKoordinaten.getLatitude(), album.getBild(rundenzaehler.getRunde() - 1)
                        .getLatitude(),markerKoordinaten.getLongitude(), album.getBild(rundenzaehler.getRunde() - 1)
                        .getLongtitude());
                this.support.firePropertyChange("Distanz", "", Formatierung.getUnit(distance));
                link();
                punkte.berechne(distance);
                this.support.firePropertyChange("Punkte", 0, punkte.getCurrentPunkteNum());
                this.support.firePropertyChange("Valid", false, true);
            } else {
                this.support.firePropertyChange("Alert", "", "Es wurde kein Marker gesetzt!");
            }
        }
    }
    public void restart() {
        stopTimer();
        rundenzaehler.restart();
        markerKoordinaten.setZero();
        support.firePropertyChange("Gesamtscore", gesamtscore.getGesamtscoreNum(), 0);
        gesamtscore.reset();
        support.firePropertyChange("Punkte", punkte.getCurrentPunkteNum(), 0);
        punkte.resetAll();
        rundeVorbei = false;
        skipMix = true;
        start();
    }
    public void koordInput(double latitude, double longitude) {
        if (!rundeVorbei) {
            if (Formatierung.isValidLongitude(longitude) && Formatierung.isValidLatitude(latitude)) {
                markerKoordinaten.setLatitude(latitude);
                markerKoordinaten.setLongitude(longitude);
                markerKoordinaten.setIsPlaced(true);
                this.support.firePropertyChange("UserMarker", null, markerKoordinaten);
            } else {
                this.support.firePropertyChange("Alert", "", "Eingegebene Werte sind nicht korrekt!");
            }
        }
    }
    public void link() {
        String link = "https://www.openstreetmap.org/directions?engine=fossgis_valhalla_foot&route="
                + markerKoordinaten.getLatitude() + KOMMA + markerKoordinaten.getLongitude() + ";"
                + album.getBild(rundenzaehler.getRunde() - 1).getLatitude() + KOMMA
                + album.getBild(rundenzaehler.getRunde() - 1).getLongtitude();
        this.link.create(link);
        this.support.firePropertyChange("Link", null, this.link);
    }

    public boolean isRundeVorbeiWithMarker() {
        return rundeVorbei && markerKoordinaten.isPlaced();
    }
    public Rundenzaehler getRundenZaehler() {
        return rundenzaehler;
    }
    public Album getAlbum() {
        return album;
    }
    public String getGesamtscoreString() {
        return gesamtscore.getStringGesamtscore();
    }
    public Punkte getPunkte() {
        return punkte;
    }
    public boolean markerIsPlaced() {
        return markerKoordinaten.isPlaced();
    }
    //Basiert auf https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            rundeVorbei = true;
        }
    }
    //Basiert auf https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    public void startTimer() {
        timer = new Timer();
        counter = rundenzaehler.getRundenlaenge();
        timerTask = new TimerTask() {
            public void run() {
                counter--;
                support.firePropertyChange("Timer", counter +1 , counter);
                if (counter < 1) {
                    support.firePropertyChange("Alert", "" , "Zeit Vorbei!");
                    if (markerKoordinaten.isPlaced()) {
                        eingabeValidieren();
                    } else {
                        stopTimer();
                    }
                }
            }
        };
        timer.schedule(timerTask, 1, SEKUNDE);
    }
    public void setAlbenListe (AlbenListe albenListe) {
        this.albenListe = albenListe;
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
