package com.gse23.lhusemann;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    private static final String RUNDE = "Runde";
    private static final String BILD = "Bild";
    private static final String GESAMTSCORE = "Gesamtscore";
    private static final String PUNKTE = "Punkte";
    private static final String LINK = "Link";
    private static final String DISTANZ = "Distanz";
    private static final String BESCHREIBUNG = "Beschreibung";
    private static final String ALERT = "Alert";

    private PropertyChangeSupport support;
    private Album album;
    private MarkerKoordinaten markerKoordinaten;
    private Rundenzaehler rundenzaehler;
    private Gesamtscore gesamtscore;
    private Punkte punkte;
    private Timer timer;
    private AlbenListe albenListe;
    private Link link;
    private boolean skipMix = false;
    private boolean rundeVorbei = false;
    private int counter;

    Game() {
        support = new PropertyChangeSupport(this);
        album = new Album();
        markerKoordinaten = new MarkerKoordinaten(0.0, 0.0, false);
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
        support.firePropertyChange(RUNDE, "", rundenzaehler.getRunde());
        support.firePropertyChange(BILD, null, album.getBild(rundenzaehler.getRunde() - 1));
        support.firePropertyChange(GESAMTSCORE, null, 0);
        support.firePropertyChange(PUNKTE, null, 0);
        link.reset();
        support.firePropertyChange(LINK, null, link);
        startTimer();
    }
    public void nextRound() {
        stopTimer();
        gesamtscore.incGesamtscore(punkte.getCurrentPunkteNum());
        support.firePropertyChange(GESAMTSCORE, gesamtscore.getGesamtscoreNum() - punkte.getCurrentPunkteNum(),
                gesamtscore.getGesamtscoreNum());

        punkte.safeCurrentPunkte();
        support.firePropertyChange(PUNKTE, punkte.getCurrentPunkteNum(), 0);
        punkte.resetCurrent();

        link.reset();
        support.firePropertyChange(LINK, null, link);

        // prüft ob Runde noch nicht vorbei
        if (rundenzaehler.incRoundIfAble()) {
            startTimer();
            rundeVorbei = false;
            markerKoordinaten.setIsPlaced(false);
            support.firePropertyChange("markerDisappear", true, false);
            support.firePropertyChange(DISTANZ, "", "keine Schaetzung abgegeben");
            support.firePropertyChange(BESCHREIBUNG, album.getBild(rundenzaehler.getRunde() - 2)
                    .getDescription(), "");
            support.firePropertyChange(RUNDE, rundenzaehler.getRunde() - 1, rundenzaehler.getRunde());
            support.firePropertyChange(BILD, album.getBild(rundenzaehler.getRunde() - 2), album
                    .getBild(rundenzaehler.getRunde() - 1));
        } else  {
            support.firePropertyChange("EndGame", true, false);
        }
    }
    public void aufgeben() {
        stopTimer();
        rundenzaehler.reset();
        markerKoordinaten.setZero();
        support.firePropertyChange(GESAMTSCORE, gesamtscore.getGesamtscoreNum(), 0);
        gesamtscore.reset();
        support.firePropertyChange(PUNKTE, punkte.getCurrentPunkteNum(), 0);
        punkte.resetAll();
        album.reset();
        album.getSize();
    }
    public void eingabeValidieren() {
        if (rundeVorbei) {
            this.support.firePropertyChange(ALERT, "", "Runde vorbei!");
        } else {
            if (markerKoordinaten.isPlaced()) {
                stopTimer();
                support.firePropertyChange(BESCHREIBUNG, "", album.getBild(rundenzaehler.getRunde() - 1)
                        .getDescription());

                this.support.firePropertyChange("desiredMarker", null, new MarkerKoordinaten(album
                        .getBild(rundenzaehler.getRunde() - 1).getLongtitude(), album.getBild(rundenzaehler
                        .getRunde() - 1).getLatitude(), true));
                double distance = Distanz.calcDistanceNum(markerKoordinaten.getLatitude(), album
                        .getBild(rundenzaehler.getRunde() - 1).getLatitude(), markerKoordinaten.getLongitude(),
                        album.getBild(rundenzaehler.getRunde() - 1).getLongtitude());
                this.support.firePropertyChange(DISTANZ, "", Formatierung.getUnit(distance));
                link();
                punkte.berechne(distance);
                this.support.firePropertyChange(PUNKTE, 0, punkte.getCurrentPunkteNum());
                this.support.firePropertyChange("Valid", false, true);
            } else {
                this.support.firePropertyChange(ALERT, "", "Es wurde kein Marker gesetzt!");
            }
        }
    }
    public void restart() {
        stopTimer();
        rundenzaehler.restart();
        markerKoordinaten.setZero();
        support.firePropertyChange(GESAMTSCORE, gesamtscore.getGesamtscoreNum(), 0);
        gesamtscore.reset();
        support.firePropertyChange(PUNKTE, punkte.getCurrentPunkteNum(), 0);
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
                this.support.firePropertyChange(ALERT, "", "Eingegebene Werte sind nicht korrekt!");
            }
        }
    }
    public void link() {
        final String komma = ",";
        String link = "https://www.openstreetmap.org/directions?engine=fossgis_valhalla_foot&route="
                + markerKoordinaten.getLatitude() + komma + markerKoordinaten.getLongitude() + ";"
                + album.getBild(rundenzaehler.getRunde() - 1).getLatitude() + komma
                + album.getBild(rundenzaehler.getRunde() - 1).getLongtitude();
        this.link.create(link);
        this.support.firePropertyChange(LINK, null, this.link);
    }
    public void startResult() {
        this.support.firePropertyChange("GesamtscoreOverlay", null, gesamtscore.getStringGesamtscore());
        this.support.firePropertyChange("ScoreOverlay", null, punkte.giveAllScores());
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
    public void setDifficulty(Punkte punkte) {
        this.punkte = punkte;
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
        final int sekunde = 1000;
        timer = new Timer();
        counter = rundenzaehler.getRundenlaenge();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                counter--;
                support.firePropertyChange("Timer", counter + 1, counter);
                if (counter < 1) {
                    support.firePropertyChange(ALERT, "", "Zeit Vorbei!");
                    if (markerKoordinaten.isPlaced()) {
                        eingabeValidieren();
                    } else {
                        stopTimer();
                    }
                }
            }
        };
        timer.schedule(timerTask, 1, sekunde);
    }
    public void setAlbenListe(AlbenListe albenListe) {
        this.albenListe = albenListe;
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
