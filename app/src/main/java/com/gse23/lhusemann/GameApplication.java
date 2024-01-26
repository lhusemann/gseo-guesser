package com.gse23.lhusemann;

import android.app.Application;

public class GameApplication extends Application {
    private Game spiel;
    public Game getGame() {
        return spiel;
    }
    public void setGame(Game spiel) {
        this.spiel = spiel;
    }

}
