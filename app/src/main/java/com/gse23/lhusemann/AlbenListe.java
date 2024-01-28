package com.gse23.lhusemann;

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

    public String[] getNames() {
        String[] names = new String[albums.length + 1];
        names[0] = "Keine Auswahl";
        for (int i = 0; i < albums.length; i++) {
            names[i + 1] = albums[i].name;
        }
        return names;
    }
}
