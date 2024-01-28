package com.gse23.lhusemann;

public class Rundenzaehler {
    private int runde = 1;
    private int maxRunde = 0;
    private int rundenlaenge = 0;
    public Rundenzaehler() {
    }
    public boolean incRoundIfAble() {
        runde++;
        return maxRunde >= runde;
    }
    public int getRunde() {
        return runde;
    }
    public void setMaxRunde(int maxRunde) {
        this.maxRunde = maxRunde;
    }
    public int getMaxRunde() {
        return maxRunde;
    }
    public void setRundenlaenge(int rundenlaenge) {
        this.rundenlaenge = rundenlaenge;
    }
    public int getRundenlaenge() {
        return rundenlaenge;
    }
    public void reset() {
        runde = 1;
        maxRunde = 0;
        rundenlaenge = 0;
    }
    public void restart() {
        runde = 1;
    }
}
