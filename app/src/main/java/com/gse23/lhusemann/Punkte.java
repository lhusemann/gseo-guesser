package com.gse23.lhusemann;

import java.util.ArrayList;

public class Punkte {
    final double minDistance = 10.0;
    final double maxDistance = 10000.0;
    final int maxPoints = 5000;
    int punkte = 0;
    ArrayList<Integer> punkteListe = new ArrayList<>();
    Punkte() {
    }
    public void berechne(double distanz) {
        if (distanz < minDistance) {
            punkte = maxPoints;
        } else if (distanz > maxDistance) {
            punkte = 0;
        } else {
            punkte = (int) ((int) (maxPoints / Math.log(maxDistance / minDistance)) * Math.log(maxDistance / distanz));
        }
    }
    public int getCurrentPunkteNum() {
        return punkte;
    }
    public String getCurrentPunkteString() {
        return "" + punkte;
    }
    public void resetCurrent() {
        punkte = 0;
    }
    public void safeCurrentPunkte() {
        punkteListe.add(punkte);
    }
    public ArrayList<Integer> giveAllScores() {
        return punkteListe;
    }
    public void resetAll() {
        punkteListe.clear();
        punkte = 0;
    }
}
