package com.gse23.lhusemann;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class AlbenListe {

    protected String success;
    protected String message;
    protected Albums[] albums;

    public AlbenListe() {
    }
    public AlbenListe(String success, String message, Albums[] albums) {
        this.success = success;
        this.message = message;
        this.albums = albums;
    }

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Albums[] getAlbums() {
        return albums;
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (IOException e) {
            return null;
        }
    }

    public static AlbenListe fromJson(final String json) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, AlbenListe.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getNames() {
        String[] names = new String[albums.length+1];
        names[0] = "keine auswahl";
        for(int i = 0; i< albums.length;i++) {
            names[i+1] = albums[i].name;
        }
        return names;
    }
}
