package com.gse23.lhusemann;

import android.app.Application;
import java.util.concurrent.ExecutorService;

public class GameApplication extends Application {
    private MarkerKoordinaten markerK;
    private Rundenzaehler runde;
    private Gesamtscore score;
    private Punkte punkte;
    private Album album;
    private Distanz distance;
    private ExecutorService service;
    private AlbenListe albenListe;
    private Game spiel;
    public MarkerKoordinaten getMarkerKoordinaten() {
        return markerK;
    }
    public void setMarkerKoordinaten(MarkerKoordinaten markerK) {
        this.markerK = markerK;
    }
    public Rundenzaehler getRundenZaehler() {
        return runde;
    }
    public void setRundezaehler(Rundenzaehler runde) {
        this.runde = runde;
    }
    public Gesamtscore getGesamtscore() {
        return score;
    }
    public void setGesamtscore(Gesamtscore score) {
        this.score = score;
    }
    public Punkte getPunkte() {
        return punkte;
    }
    public void setPunkte(Punkte punkte) {
        this.punkte = punkte;
    }
    public Album getAlbum() {
        return album;
    }
    public void setAlbum(Album album) {
        this.album = album;
    }
    public Distanz getDistanz() {
        return distance;
    }
    public void setDistanz(Distanz distance) {
        this.distance = distance;
    }
    public ExecutorService getService() {
        return service;
    }
    public void setService(ExecutorService service) {
        this.service = service;
    }
    public AlbenListe getAlbenListe() {
        return albenListe;
    }
    public void setAlbenListe(AlbenListe albenListe) {
        this.albenListe = albenListe;
    }
    public Game getGame() {
        return spiel;
    }
    public void setGame(Game spiel) {
        this.spiel = spiel;
    }
}
