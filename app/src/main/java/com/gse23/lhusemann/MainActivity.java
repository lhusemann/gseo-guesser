package com.gse23.lhusemann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.IOException;

import java.io.InputStream;

import androidx.exifinterface.media.ExifInterface;

/**
 * Das ist die MainActivity, die zuerst ausgeführt wird, wenn die App gestartet wird.
 */
public class MainActivity extends AppCompatActivity {
    //wenn ich die nicht verwende, meckert der StyleGuide
    String nichtVorhanden = "nicht vorhanden";
    boolean keineAuswahl = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = findViewById(R.id.spinner);
        Button startButton = findViewById(R.id.startButton);
        String error = "Kein Ordner ausgewaehlt";
        try {
            String album = "albums";
            String[] empty = new String[1];
            empty[0] =  error;
            if (getAssets().list(album).length < 1) {
                Log.i("Fehler", "Keine Dateien in albums vorhanden");
                ArrayAdapter adapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, empty);
                spinner.setAdapter(adapter);

            } else {
                this.showAllImagesInAlbums(album);
                String[] auswahlmoeglichkeiten = this.mergeArray(empty, getAssets().list(album));
                ArrayAdapter adapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, auswahlmoeglichkeiten);
                spinner.setAdapter(adapter);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //prüft ob etwas Aufgewaehlt ist
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner.getSelectedItem().toString().equals(error)) {
                    keineAuswahl = true;
                } else {
                    keineAuswahl = false;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                keineAuswahl = true;
            }
        });
        startButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (keineAuswahl) {
                    AlertDialog.Builder error = new AlertDialog.Builder(MainActivity.this);
                    error.setMessage("Du hast keinen Ordner ausgewählt!")
                            .setPositiveButton("Ok", (dialog, id) -> {})
                            .setTitle("Error");
                    AlertDialog alert = error.create();
                    alert.show();
                } else {
                    Intent intent = new Intent(MainActivity.this, SpielansichtActivity.class);
                    intent.putExtra("album", (spinner.getSelectedItem().toString()));
                    startActivity(intent);
                }
            }
        });
    }
    //gibt Inhalt eines Ordner wieder
    private String[] getFolderInhalt(String folder) throws IOException {
        return getAssets().list(folder);
    }
    //gibt alle Bilder mit Exif-Daten im Log aus
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
    //prüft ob es sich um eine Bild-Datei handelt
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
    //Hilfsfunktion um von allen Ordnern den Inhalt anzeigen zu lassen
    private void showAllImagesInAlbums(String album) throws IOException {
        String[] alben = this.getFolderInhalt(album);
        for (String folder : alben) {
            this.showAllImagesInFolder(getFolderInhalt(album + "/" + folder), folder);
        }
    }
    //gibt Längengrad aus
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
    //gibt Breitengrad aus
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
    //gibt Beschreibung aus
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
    //https://www.javatpoint.com/how-to-merge-two-arrays-in-java#:~:text=Java%20arraycopy()%20method&text=It%20copies%20an%20array%20from,equal%20to%20the%20length%20argument
    String[] mergeArray(String[] firstArray, String[] secondArray) {
        int firstLength = firstArray.length;
        int secondLength = secondArray.length;
        String[] result = new String[firstLength + secondLength];
        System.arraycopy(firstArray, 0, result, 0, firstLength);
        System.arraycopy(secondArray, 0, result, firstLength, secondLength);
        return result;
    }
}
