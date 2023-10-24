package com.gse23.lhusemann;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;

import java.io.IOException;

import java.io.InputStream;

import androidx.exifinterface.media.ExifInterface;

/**
 * Das ist die MainActivity, die zuerst ausgeführt wird, wenn die App gestartet wird.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            String album = "albums";
            if (getAssets().list(album).length < 1) {
                Log.i("Fehler", "Keine Dateien in albums vorhanden");
            } else {
                this.showAllImagesInAlbums(album);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getFolderInhalt(String folder) throws IOException {
        return getAssets().list(folder);
    }
    private void showAllImagesInFolder(String[] files, String folder) {
        for (String file : files) {
            if (file.contains(".jpeg") || file.contains(".jpg") || file.contains(".png")) {
                Log.i("Foto-Pfad", "Im Ordner " + folder + " liegt das Foto: " + file);
                Log.i("Foto-Laengengrad", "Laengengrad: " + this.getLaengengrad("albums/" + folder + "/" + file));
                Log.i("Foto-Breitengrad", "Breitengrad: " + this.getBreitengrad("albums/" + folder + "/" + file));
                Log.i("Beschreibung", "Beschreibung: " + this.getBeschreibung("albums/" + folder + "/" + file));
            }
        }
    }
    private void showAllImagesInAlbums(String album) throws IOException {
        String[] alben = this.getFolderInhalt(album);
        for (String folder : alben) {
            this.showAllImagesInFolder(getFolderInhalt(album + "/" + folder), folder);
        }
    }
    private String getLaengengrad(String filepath) {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            return exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        } catch (IOException e) {
            return null;
        }
    }
    private String getBreitengrad(String filepath) {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            return exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        } catch (IOException e) {
            return null;
        }
    }
    private String getBeschreibung(String filepath) {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            return exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        } catch (IOException e) {
            return null;
        }
    }
}
