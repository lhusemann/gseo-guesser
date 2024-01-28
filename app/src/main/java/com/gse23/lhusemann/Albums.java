package com.gse23.lhusemann;

public class Albums {
    protected String id;
    protected String link;
    protected String name;
    protected int numImages;

    public Albums() {
    }
    public Albums(String id, String link, String name, int numImages) {
        this.id = id;
        this.link = link;
        this.name = name;
        this.numImages = numImages;
    }


    public String getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }
    public int getNumImages() {
        return numImages;
    }

}
