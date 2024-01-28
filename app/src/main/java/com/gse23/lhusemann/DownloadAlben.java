package com.gse23.lhusemann;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class DownloadAlben {
    public static AlbenListe jsonToAlbenListe(final String json) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, AlbenListe.class);
        } catch (IOException e) {
            return null;
        }
    }
    public static ServerAlbum jsonToServerAlbum(final String json) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, ServerAlbum.class);
        } catch (IOException e) {
            return null;
        }
    }
}
