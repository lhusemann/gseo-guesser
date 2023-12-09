package com.gse23.lhusemann;

public class Punkte {
    final double minDistance = 10.0;
    final double maxDistance = 10000.0;
    final int maxPoints = 5000;
    int punkte;
    Punkte() {
        punkte = 0;
    }
    public double berechne(double distanz) {
        if (distanz < minDistance) {
            punkte = maxPoints;
        } else if (distanz > maxDistance) {
            punkte = 0;
        } else {
            punkte = (int) ((int) (maxPoints / Math.log(maxDistance / minDistance)) * Math.log(maxDistance / distanz));
        }
        return punkte;
    }
    public int getPunkte() {
        return punkte;
    }
    public String getStringPunkte() {
        return "" + punkte;
    }
    public void reset() {
        punkte = 0;
    }
}
