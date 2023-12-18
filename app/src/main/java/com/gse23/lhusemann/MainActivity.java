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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    Button updateAnsichtButton;
    EditText rundenAnzahl;
    EditText rundenLaenge;
    String album;
    List<String> auswahlmoeglichkeitenListe;
    ArrayAdapter<String> auswahlAdapter;
    boolean keineAuswahl = true;
    boolean validR;
    boolean validL;
    boolean ignoreCauseNoImage = false;
    int indexDownloaded = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameApplication = (GameApplication) getApplication();
        gameApplication.setRundezaehler(new Rundenzaehler(0,-1,0));
        gameApplication.getRundenZaehler().addListener(this);
        gameApplication.setAlbum(new Album(""));
        gameApplication.getAlbum().addListener(this);
        spinner = findViewById(R.id.spinner);
        startButton = findViewById(R.id.startButton);
        downloadAnsichtButton = findViewById(R.id.downloadViewButton);
        updateAnsichtButton = findViewById(R.id.updateViewButton);
        rundenAnzahl = findViewById(R.id.rundenanzahlField);
        rundenLaenge = findViewById(R.id.rundenlaengeField);
        auswahlmoeglichkeitenListe = new ArrayList<>();
        album = "albums";
        auswahlAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,auswahlmoeglichkeitenListe);
        spinner.setAdapter(auswahlAdapter);

        //prüft ob etwas Ausgewaehlt ist
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                keineAuswahl = position == 0;
                if (keineAuswahl) {
                    rundenAnzahl.setHint("0");
                } else {
                    gameApplication.getAlbum().setDownloaded(position > indexDownloaded);
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
                            gameApplication.getRundenZaehler().setMaxRunde(5);
                        }
                    } else {
                        gameApplication.getRundenZaehler().setMaxRunde(Integer.parseInt(rundenAnzahl.getText().toString()));
                    }
                    if (ignoreCauseNoImage) {
                        gameApplication.getRundenZaehler().setMaxRunde(2);
                    }
                    if (rundenLaenge.getText().toString().equals("")) {
                        gameApplication.getRundenZaehler().setRundenlaenge(30);
                    } else {
                        gameApplication.getRundenZaehler().setRundenlaenge(Integer.parseInt(rundenLaenge.getText().toString()));
                    }
                    if (validL && validR) {
                        gameApplication.getAlbum().setName(spinner.getSelectedItem().toString());
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
        updateAnsichtButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                auswahlAdapter.clear();
                auswahlmoeglichkeitenListe.add("Kein Ordner ausgewaehlt");
                try {
                    if (getAssets().list(album) == null || getFolderInhalt(album).length < 1) {
                    } else {
                        auswahlmoeglichkeitenListe.addAll(Arrays.asList(getFolderInhalt(album)));
                        indexDownloaded = getFolderInhalt(album).length;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                addDownloads();
                auswahlAdapter.notifyDataSetChanged();
            }
        });
        updateAnsichtButton.performClick();
        showAllImagesInAlbums();
    }
    private void addDownloads() {
        File downloadOrdner = new File(getFilesDir() + "/Album");
        if (downloadOrdner.exists() && downloadOrdner.isDirectory()) {
            File[] dateien = downloadOrdner.listFiles();
            if (dateien != null) {
                for (File album : dateien) {
                    if (album.isDirectory()) {
                        auswahlmoeglichkeitenListe.add(album.getName() + " (Heruntergeladen)");
                    }
                }
            }
        }
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
            if ((int) propertyChangeEvent.getNewValue() > getImagesCount(spinner.getSelectedItem().toString()) && !ignoreCauseNoImage) {
                Toast.makeText(this, "Rundenanzahl uebertrifft Bilder in Album!", Toast.LENGTH_SHORT).show();
                validR = false;
            } else if ((int) propertyChangeEvent.getNewValue() < 1) {
                if (getImagesCount(spinner.getSelectedItem().toString()) == 0) {
                    ignoreCauseNoImage = true;
                } else {
                    ignoreCauseNoImage = false;
                    Toast.makeText(this, "Du musst min. eine Runde spielen!", Toast.LENGTH_SHORT).show();
                    validR = false;
                }
            } else {
                validR = true;
            }
        }
        if (changeName.equals("rundenlaenge")) {
            if ((int) propertyChangeEvent.getNewValue() > 60) {
                Toast.makeText(this, "Eine Runde kann maximal 60 Sekunden lang sein", Toast.LENGTH_SHORT).show();
                validL = false;
            } else if ((int) propertyChangeEvent.getNewValue() < 10) {
                Toast.makeText(this, "Eine Runde muss mindestens 10 Sekunden lang sein", Toast.LENGTH_SHORT).show();
                validL = false;
            } else {
                validL = true;
            }
        }
        if (changeName.equals("name")) {
            Intent intent = new Intent(MainActivity.this, SpielansichtActivity.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onDestroy() {
        gameApplication.getRundenZaehler().removeListener(this);
        gameApplication.getAlbum().removeListener(this);
        super.onDestroy();
    }
}

