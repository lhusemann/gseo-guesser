package com.gse23.lhusemann;

public class Link {
    String link;
    String color;
    private static final String NOGUESS = "Kein Guess abgegeben.";
    private static final String defaultGray = "#808080";
    private static final String defaultBlue = "#0000EE";
    Link() {
        link = NOGUESS;
        color = defaultGray;
    }
    public void create(String link) {
        this.link = link;
        color = defaultBlue;
    }
    public void reset() {
        link = NOGUESS;
        color = defaultGray;
    }
    public String getLink() {
        return link;
    }
    public String getColor() {
        return color;
    }
}
