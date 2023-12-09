package com.gse23.lhusemann;

import android.app.Application;

public class GameApplication extends Application {
    private MarkerKoordinaten markerK;
    private Rundenzaehler runde;
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
}
