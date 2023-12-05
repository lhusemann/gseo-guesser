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
    String album = "albums";
    boolean keineAuswahl = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = findViewById(R.id.spinner);
        Button startButton = findViewById(R.id.startButton);
        String error = "Kein Ordner ausgewaehlt";
        try {
            String[] empty = new String[1];
            empty[0] =  error;
            if (getAssets().list(album) == null || getFolderInhalt(album).length < 1) {
                Log.i("Fehler", "Keine Dateien in albums vorhanden");
                spinner.setAdapter(new ArrayAdapter(this,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, empty));
            } else {
                this.showAllImagesInAlbums();
                String[] auswahlmoeglichkeiten = this.mergeArray(empty, getFolderInhalt(album));
                spinner.setAdapter(new ArrayAdapter(this,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, auswahlmoeglichkeiten));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //prüft ob etwas Ausgewaehlt ist
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                keineAuswahl = position == 0;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                keineAuswahl = true;
            }
        });
        //Wenn der Button gedrückt wird wird entweder eine Fehlernachricht ausgegeben oder das Spiel gestartet
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
                    Log.i("Ausgewahl", "Das Album " + spinner.getSelectedItem().toString() + " wurde ausgewaehlt.");
                    Intent intent = new Intent(MainActivity.this, SpielansichtActivity.class);
                    intent.putExtra("ausgewaehltesAlbum", spinner.getSelectedItem().toString());
                    intent.putExtra("anzahlBilder", getImagesCount(spinner.getSelectedItem().toString()));
                    startActivity(intent);
                }
            }
        });
    }
    //gibt Inhalt eines Ordner wieder ohne Fehlermeldung
    private String[] getFolderInhalt(String folder) {
        try {
            return getAssets().list(folder);
        } catch (IOException e) {
            return new String[0];
        }
    }
    //prüft ob es sich um eine Bild-Datei handelt
    private boolean isImage(String name) {
        return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
    }
    //gibt alle Bilder mit Exif-Daten im Log aus
    private void showAllImagesInAlbums() {
        for (String folder : this.getFolderInhalt(album)) {
            for (String file : getFolderInhalt(album + "/" + folder)) {
                if (this.isImage(file)) {
                    String path = album + "/" + folder + "/" + file;
                    Log.i("Foto-Pfad", "Im Ordner " + folder + " liegt das Foto: " + file);
                    Log.i("Laengengrad", this.getExifdata(path, ExifInterface.TAG_GPS_LONGITUDE));
                    Log.i("Breitengrad", this.getExifdata(path, ExifInterface.TAG_GPS_LATITUDE));
                    Log.i("Beschreibung", this.getExifdata(path, ExifInterface.TAG_IMAGE_DESCRIPTION));
                }
            }
        }
    }
    //gibt Exifdaten aus
    private String getExifdata(String filepath, String exifTag) {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            if (exifInterface.getAttribute(exifTag) == null) {
                return "nicht vorhanden";
            } else {
                return exifInterface.getAttribute(exifTag);
            }
        } catch (IOException e) {
            return "Das öffnen des InputsStreams hat nicht funktioniert";
        }
    }
    /**
     * habe arraycopy auf dieser Website gefunden und das Beispiel so umgeschrieben
     * dass es meinen Zweck erfüllt: https://www.javatpoint.com/how-to-merge-two-arrays-in-java#:~:text=
     * Java%20arraycopy()%20method&text=It%20copies%20an%20array%20from,equal%20to%20the%20length%20argument
     */
    String[] mergeArray(String[] firstArray, String[] secondArray) {
        int firstLength = firstArray.length;
        int secondLength = secondArray.length;
        String[] result = new String[firstLength + secondLength];
        System.arraycopy(firstArray, 0, result, 0, firstLength);
        System.arraycopy(secondArray, 0, result, firstLength, secondLength);
        return result;
    }
    //gibt anzahl der Bilder in einem Ordner wieder
    private int getImagesCount(String folder) {
        int counter = 0;
        if (album == null || folder == null) {
            return 0;
        }
        for (String file : getFolderInhalt(album + "/" + folder)) {
            if (this.isImage(file)) {
                counter++;
            }
        }
        return counter;
    }
}
