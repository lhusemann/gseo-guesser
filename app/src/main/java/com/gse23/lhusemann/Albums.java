package com.gse23.lhusemann;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

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

    public static Albums fromJson(final String json) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, Albums.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
