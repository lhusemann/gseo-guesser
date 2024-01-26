package com.gse23.lhusemann;

import java.util.ArrayList;
import java.util.Collections;

public class Album {
    ArrayList<Bild> album = new ArrayList<>();
    String name = "";

    public Album() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public int getSize() {
        return album.size();
    }

    public void addBild(Bild neu) {
        album.add(neu);
    }

    public Bild getBild(int index) {
        if (album.size() >= index) {
            return album.get(index);
        } else {
            return null;
        }
    }

    public void mix() {
        Collections.shuffle(album);
    }

    public void reset() {
        name = "";
        album.clear();
    }
}
