package com.gse23.lhusemann;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;

import java.io.InputStream;

import androidx.exifinterface.media.ExifInterface;

/**
 * Das ist die MainActivity, die zuerst ausgeführt wird, wenn die App gestartet wird.
 */
public class MainActivity extends AppCompatActivity {
    String nichtVorhanden = "nicht vorhanden";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = findViewById(R.id.spinner);
        try {
            String album = "albums";
            if (getAssets().list(album).length < 1) {
                Log.i("Fehler", "Keine Dateien in albums vorhanden");
                String[] empty = new String[1];
                empty[0]="keine Ordner enthalten";
                ArrayAdapter adapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,empty);
                spinner.setAdapter(adapter);

            } else {
                this.showAllImagesInAlbums(album);
                ArrayAdapter adapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,getAssets().list(album));
                spinner.setAdapter(adapter);
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
            if (this.isImage(file)) {
                Log.i("Foto-Pfad", "Im Ordner " + folder + " liegt das Foto: " + file);
                String album = "albums/";
                Log.i("Foto-Laengengrad", "Laengengrad: " + this.getLaengengrad(album + folder + "/" + file));
                Log.i("Foto-Breitengrad", "Breitengrad: " + this.getBreitengrad(album + folder + "/" + file));
                Log.i("Beschreibung", "Beschreibung: " + this.getBeschreibung(album + folder + "/" + file));
            }
        }
    }
    private boolean isImage(String name) {
        int laenge = name.length();
        final int three = 3;
        final int four = 4;
        final int five = 5;
        if (name.charAt(laenge - 1) == 'g') {
            if (name.charAt(laenge - 2) == 'n') {
                if (name.charAt(laenge - three) == 'p') {
                    if (name.charAt(laenge - four) == '.') {
                        return true;
                    }
                }
            }
            if (name.charAt(laenge - 2) == 'p') {
                if (name.charAt(laenge - three) == 'j') {
                    if (name.charAt(laenge - four) == '.') {
                        return true;
                    }
                }
            }
            if (name.charAt(laenge - 2) == 'e') {
                if (name.charAt(laenge - three) == 'p') {
                    if (name.charAt(laenge - four) == 'j') {
                        if (name.charAt(laenge - five) == '.') {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
            if (exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE) == null) {
                return nichtVorhanden;
            } else {
                return exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            }
        } catch (IOException e) {
            return null;
        }
    }
    private String getBreitengrad(String filepath) {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            if (exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) == null) {
                return nichtVorhanden;
            } else {
                return exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            }
        } catch (IOException e) {
            return null;
        }
    }
    private String getBeschreibung(String filepath) {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            if (exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION) == null) {
                return nichtVorhanden;
            } else {
                return exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
            }
        } catch (IOException e) {
            return null;
        }
    }
}
