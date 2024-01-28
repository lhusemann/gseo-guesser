package com.gse23.lhusemann;

public class Link {
    private static final String NOGUESS = "Kein Guess abgegeben.";
    private static final String DEFAULTGRAY = "#808080";
    private static final String DEFAULTBLUE = "#0000EE";

    String link;
    String color;
    Link() {
        link = NOGUESS;
        color = DEFAULTGRAY;
    }
    public void create(String link) {
        this.link = link;
        color = DEFAULTBLUE;
    }
    public void reset() {
        link = NOGUESS;
        color = DEFAULTGRAY;
    }
    public String getLink() {
        return link;
    }
    public String getColor() {
        return color;
    }
}
