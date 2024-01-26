package com.gse23.lhusemann;

public class Gesamtscore {
    private int score = 0;
    public Gesamtscore() {
    }
    public void incGesamtscore(int score) {
        this.score += score;
    }
    public int getGesamtscoreNum() {
        return score;
    }
    public String getStringGesamtscore() {
        return "" + score;
    }
    public void reset() {
        score = 0;
    }
}
