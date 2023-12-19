package com.gse23.lhusemann;

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
    boolean notResume = true;
    int indexDownloaded = 0;
    //Diese Methode wird beim Start ausgeführt
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
        //wenn im Spinner etwas neues ausgewählt wird, wird hier damit umgegangen
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                keineAuswahl = position == 0;
                if (keineAuswahl) {
                    rundenAnzahl.setHint("0");
                } else {
                    gameApplication.getAlbum().setDownloaded(position > indexDownloaded);
                    if (gameApplication.getAlbum().isDownloaded()) {
                        gameApplication.getAlbum().setName((spinner.getSelectedItem().toString()).substring(0, (spinner.getSelectedItem().toString()).length() - 18));
                    } else {
                        gameApplication.getAlbum().setName((spinner.getSelectedItem().toString()));
                    }
                    if (getImagesCount(gameApplication.getAlbum().getName())<5) {
                        rundenAnzahl.setHint("" + getImagesCount(gameApplication.getAlbum().getName()));
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
        //Wenn der Start-Button gedrückt wird wird entweder eine Fehlernachricht ausgegeben oder das Spiel gestartet
        startButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (keineAuswahl) {
                    Snackbar.make(button, "Du hast keinen Ordner ausgewählt!" , Snackbar.LENGTH_SHORT).show();
                } else {
                    if (rundenAnzahl.getText().toString().equals("")) {
                        gameApplication.getRundenZaehler().setMaxRunde(Math.min(getImagesCount(gameApplication.getAlbum().getName()), 5));
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
                        Intent intent = new Intent(MainActivity.this, SpielansichtActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
        //Mit diesem Button gelangt man in die Downloadansicht
        downloadAnsichtButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                startActivity(intent);
            }
        });
        //Mit diesem Button wird der Spinner geupdated
        updateAnsichtButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                auswahlAdapter.clear();
                auswahlmoeglichkeitenListe.add("Kein Ordner ausgewaehlt");
                try {
                    if (getAssets().list(album) != null && ReadExif.getFolderInhalt(album, MainActivity.this, false).length > 0) {
                        auswahlmoeglichkeitenListe.addAll(Arrays.asList(ReadExif.getFolderInhalt(album, MainActivity.this, false)));
                        indexDownloaded = ReadExif.getFolderInhalt(album, MainActivity.this, false).length;
                    }
                } catch (IOException e) {
                    return;
                }
                addDownloads();
                auswahlAdapter.notifyDataSetChanged();
            }
        });
        updateAnsichtButton.performClick();
        showAllImagesInAlbums();
    }
    //Eine HilfsMethode, die die Alben aus dem internen Speicher zum Spinner hinzufügt
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
    //prüft ob es sich um eine Bild-Datei handelt
    private boolean isImage(String name) {
        return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
    }
    //gibt alle Bilder mit Exif-Daten der Alben in Assets-Ordner im Log aus
    private void showAllImagesInAlbums() {
        for (String folder : ReadExif.getFolderInhalt(album, MainActivity.this, false)) {
            for (String file : ReadExif.getFolderInhalt(album + "/" + folder, MainActivity.this, false)) {
                if (this.isImage(file)) {
                    String path = album + "/" + folder + "/" + file;
                    Log.i("Foto-Pfad", "Im Ordner " + folder + " liegt das Foto: " + file);
                    ReadExif.logExifAssets(path, ExifInterface.TAG_GPS_LONGITUDE, MainActivity.this);
                    ReadExif.logExifAssets(path, ExifInterface.TAG_GPS_LATITUDE, MainActivity.this);
                    ReadExif.logExifAssets(path, ExifInterface.TAG_IMAGE_DESCRIPTION, MainActivity.this);
                }
            }
        }
    }
    //gibt anzahl der Bilder in einem Ordner wieder
    private int getImagesCount(String folder) { // folder ist nur der name des album!
        int counter = 0;
        String vorPath;
        if (gameApplication.getAlbum().isDownloaded()) {
            vorPath = getFilesDir() + "/Album/" + folder;
        } else {
            vorPath = album + "/" + folder;
        }
        for (String file : ReadExif.getFolderInhalt(vorPath, MainActivity.this, gameApplication.getAlbum().isDownloaded())) {
            if (this.isImage(file)) {
                counter++;
            }
        }
        return counter;
    }
    //Wenn in einem Model eine Sache verändert wird, wird hier gesagt, was passieren soll
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String changeName = propertyChangeEvent.getPropertyName();
        if (changeName.equals("maxRunde")) {
            if (notResume) {
                if ((int) propertyChangeEvent.getNewValue() > getImagesCount(gameApplication.getAlbum().getName()) && !ignoreCauseNoImage) {
                    Toast.makeText(this, "Rundenanzahl uebertrifft Bilder in Album!", Toast.LENGTH_SHORT).show();
                    validR = false;
                } else if ((int) propertyChangeEvent.getNewValue() < 1) {
                    if (getImagesCount(gameApplication.getAlbum().getName()) == 0) {
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
        }
        if (changeName.equals("rundenlaenge")) {
            if (notResume) {
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
        }
    }
    //Wenn die Activity neugeladen wird, sollen alle Werte in den Models zurückgesetzt werden
    @Override
    protected void onResume() {
        super.onResume();
        notResume = false;
        gameApplication.getRundenZaehler().setRunde(0);
        gameApplication.getRundenZaehler().setRundenlaenge(0);
        gameApplication.getRundenZaehler().setMaxRunde(-1);
        gameApplication.getAlbum().reset();
        spinner.setSelection(0, false);
        notResume = true;
    }
    //Wenn die Activity beeendet wird, müssen die Listener entfernt werden
    @Override
    protected void onDestroy() {
        gameApplication.getRundenZaehler().removeListener(this);
        gameApplication.getAlbum().removeListener(this);
        super.onDestroy();
    }
}

