package com.gse23.lhusemann;

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


    boolean rundeVorbei = false;
    private static final int SEKUNDE = 1000;
    int counter;
    private static final String KOMMA = ",";
    Game() {
        support = new PropertyChangeSupport(this);
        album = new Album();
        markerKoordinaten = new MarkerKoordinaten(0.0,0.0);
        rundenzaehler = new Rundenzaehler();
        gesamtscore = new Gesamtscore();
        punkte = new Punkte();
    }
    public void start() {
        album.mix();
        support.firePropertyChange("Runde", "", rundenzaehler.getRunde()); //aktuellisiert Runde
        support.firePropertyChange("Bild", null, album.getBild(rundenzaehler.getRunde() - 1)); //aktuellisiert Drawable von Bild
        startTimer();
    }
    public void nextRound() {
        stopTimer();
        gesamtscore.incGesamtscore(punkte.getCurrentPunkteNum());
        support.firePropertyChange("Gesamtscore", gesamtscore.getGesamtscoreNum() - punkte.getCurrentPunkteNum(), gesamtscore.getGesamtscoreNum());

        punkte.safeCurrentPunkte();

        support.firePropertyChange("Punkte", punkte.getCurrentPunkteNum(), 0);
        punkte.resetCurrent();

        // prüft ob Runde noch nicht vorbei
        if (rundenzaehler.incRoundIfAble()) {
            startTimer();
            rundeVorbei = false;
            markerKoordinaten.setIsSet(false);
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
        gesamtscore.reset();
        punkte.resetAll();
        album.reset();
    }
    public void eingabeValidieren() {
        if (rundeVorbei) {
            this.support.firePropertyChange("Alert", "", "Runde vorbei!");
        } else {
            if (markerKoordinaten.isSet()) {
                stopTimer();
                this.support.firePropertyChange("desiredLongitude", album.getBild(rundenzaehler.getRunde() - 1)
                        .getLongtitude() - 1, album.getBild(rundenzaehler.getRunde() - 1).getLongtitude());
                this.support.firePropertyChange("desiredLatitude", album.getBild(rundenzaehler.getRunde() - 1)
                        .getLatitude() - 1, album.getBild(rundenzaehler.getRunde() - 1).getLatitude());
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
        rundenzaehler.reset();
        markerKoordinaten.setZero();
        gesamtscore.reset();
        punkte.resetAll();
        start();
    }
    public void koordInput(double latitude, double longitude) {
        if (!rundeVorbei) {
            if (Formatierung.isValidLongitude(longitude) && Formatierung.isValidLatitude(latitude)) {
                markerKoordinaten.setLatitude(latitude);
                this.support.firePropertyChange("Breitengrad", latitude-1, latitude);
                markerKoordinaten.setLongitude(longitude);
                this.support.firePropertyChange("Laengengrad", longitude-1, longitude);
                markerKoordinaten.setIsSet(true);
            } else {
                this.support.firePropertyChange("Alert", "", "Eingegebene Werte sind nicht korrekt!");
            }
        }
    }
    //-------------------JOOO erm was ist wennn keine eingabe? dann link zu 0.0 gerade glaube -------------------------------------------
    public void link() {
        String link = "https://www.openstreetmap.org/directions?engine=fossgis_valhalla_foot&route="
                + markerKoordinaten.getLatitude() + KOMMA + markerKoordinaten.getLongitude() + ";"
                + album.getBild(rundenzaehler.getRunde() - 1).getLatitude() + KOMMA
                + album.getBild(rundenzaehler.getRunde() - 1).getLongtitude();
        this.support.firePropertyChange("Link", "", link);
    }

    public boolean isRundeVorbeiWithMarker() {
        return rundeVorbei && markerKoordinaten.isSet();
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
    public boolean markerIsSet() {
        return markerKoordinaten.isSet();
    }
    //Basiert auf https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            rundeVorbei = true;
            support.firePropertyChange("Beschreibung", "", album.getBild(rundenzaehler.getRunde() - 1).getDescription());//aktuellisiert Beschreibung
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
                    if (markerKoordinaten.isSet()) {
                        eingabeValidieren();
                    } else {
                        stopTimer();
                    }
                }
            }
        };
        timer.schedule(timerTask, 1, SEKUNDE);
    }

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
