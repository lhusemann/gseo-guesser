package com.gse23.lhusemann;

import java.util.ArrayList;
import java.util.Collections;

public class Album {
    ArrayList<Bild> album = new ArrayList<>();
    public Album() {
    }
    public void addBild(Bild neu) {
        album.add(neu);
    }
    public Bild getBild(int index) {
        if (album.size()>=index) {
            return album.get(index);
        } else {
            return null;
        }
    }
    public void mix() {
        Collections.shuffle(album);
    }
    public boolean isEmptyUndLogAll() throws NoImagesInAlbumException {
            if (album.isEmpty()) {
                return true;
            } else {
                for (int i = 0; i < album.size(); i++) {
                    this.logExif(i);
                }
                return false;
            }
    }
    public void logExif(int index) {
        if (album.size()>=index) {
            album.get(index).logName();
            album.get(index).logDescription();
            album.get(index).logLongtitude();
            album.get(index).logLatitude();
        }
    }
    public void logExifNoDec(int index) {
        if (album.size()>=index) {
            album.get(index).logName();
            album.get(index).logLongtitude();
            album.get(index).logLatitude();
        }
    }
}
