package com.gse23.lhusemann;

import android.app.Application;

public class GameApplication extends Application {
    private MarkerKoordinaten markerK;
    public MarkerKoordinaten getMarkerKoordinaten() {
        return markerK;
    }
    public void setMarkerKoordinaten(MarkerKoordinaten markerK) {
        this.markerK = markerK;
    }
}
