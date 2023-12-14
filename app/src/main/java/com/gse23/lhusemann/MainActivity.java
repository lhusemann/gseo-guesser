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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.material.snackbar.Snackbar;

/**
 * Das ist die MainActivity, die zuerst ausgeführt wird, wenn die App gestartet wird.
 */
public class MainActivity extends AppCompatActivity implements PropertyChangeListener {
    GameApplication gameApplication;
    Spinner spinner;
    Button startButton;
    Button downloadAnsichtButton;
    EditText rundenAnzahl;
    EditText rundenLaenge;
    String album;
    String error;
    boolean keineAuswahl = true;
    boolean valid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameApplication = (GameApplication) getApplication();
        gameApplication.setRundezaehler(new Rundenzaehler(0,5,30));
        gameApplication.getRundenZaehler().addListener(this);
        spinner = findViewById(R.id.spinner);
        startButton = findViewById(R.id.startButton);
        downloadAnsichtButton = findViewById(R.id.downloadViewButton);
        rundenAnzahl = findViewById(R.id.rundenanzahlField);
        rundenLaenge = findViewById(R.id.rundenlaengeField);
        error = "Kein Ordner ausgewaehlt";
        album = "albums";
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
                if (keineAuswahl) {
                    rundenAnzahl.setHint("0");
                } else {
                    if (getImagesCount(spinner.getSelectedItem().toString())<5) {
                        rundenAnzahl.setHint("" + getImagesCount(spinner.getSelectedItem().toString()));
                    } else {
                        rundenAnzahl.setHint("5");
                    }
                }
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
                    if (rundenAnzahl.getText().toString().equals("")) {
                        if (getImagesCount(spinner.getSelectedItem().toString())<5) {
                            gameApplication.getRundenZaehler().setMaxRunde(getImagesCount(spinner.getSelectedItem().toString()));
                        } else {
                            Log.i("fixen","ruft propertychange nicht auf, aber wenn keine laenge angegeben?");
                            gameApplication.getRundenZaehler().setMaxRunde(5);
                        }
                    } else {
                        gameApplication.getRundenZaehler().setMaxRunde(Integer.parseInt(rundenAnzahl.getText().toString()));
                    }
                    if (rundenLaenge.getText().toString().equals("")) {
                            gameApplication.getRundenZaehler().setRundenlaenge(30);
                    } else {
                        gameApplication.getRundenZaehler().setRundenlaenge(Integer.parseInt(rundenLaenge.getText().toString()));
                    }
                    if (valid) {
                        Intent intent = new Intent(MainActivity.this, SpielansichtActivity.class);
                        intent.putExtra("ausgewaehltesAlbum", spinner.getSelectedItem().toString());
                        intent.putExtra("anzahlBilder", getImagesCount(spinner.getSelectedItem().toString()));
                        Log.i("test","test");
                        startActivity(intent);
                    }
                }
            }
        });
        downloadAnsichtButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                startActivity(intent);
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

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String changeName = propertyChangeEvent.getPropertyName();
        if (changeName.equals("maxRunde")) {
            if ((int) propertyChangeEvent.getNewValue() > getImagesCount(spinner.getSelectedItem().toString())) {
                Toast.makeText(this, "Rundenanzahl uebertrifft Bilder in Album!", Toast.LENGTH_SHORT).show();
                valid = false;
            } else if ((int) propertyChangeEvent.getNewValue() < 1) {
                Toast.makeText(this, "Du musst min. eine Runde spielen!", Toast.LENGTH_SHORT).show();
                valid = false;
            } else {
                valid = true;
            }
        }
        if (changeName.equals("rundenlaenge")) {
            if ((int) propertyChangeEvent.getNewValue() > 60) {
                Toast.makeText(this, "Eine Runde kann maximal 60 Sekunden lang sein", Toast.LENGTH_SHORT).show();
                valid = false;
            } else if ((int) propertyChangeEvent.getNewValue() < 10) {
                Toast.makeText(this, "Eine Runde muss mindestens 10 Sekunden lang sein", Toast.LENGTH_SHORT).show();
                valid = false;
            } else {
                valid = true;
            }
        }
    }
    @Override
    protected void onDestroy() {
        gameApplication.getRundenZaehler().removeListener(this);
        super.onDestroy();
    }
}

