package com.gse23.lhusemann;

public class Punkte {
    final double minDistance = 10.0;
    final double maxDistance = 10000.0;
    final int maxPoints = 5000;
    int punkte = 0;
    Punkte(double distanz) {
        if (distanz < minDistance) {
            punkte = maxPoints;
        } else if (distanz > maxDistance) {
            punkte = 0;
        } else {
            punkte = (int) ((int) (maxPoints / Math.log(maxDistance / minDistance)) * Math.log(maxDistance / distanz));
        }
    }
    public int getPunkte() {
        return punkte;
    }
}
