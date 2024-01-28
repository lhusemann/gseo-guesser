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
    private static final int MINSEKUNDEN = 10;
    private static final int MAXSEKUNDEN = 60;
    private static final int SDTLAENGE = 30;
    private static final int SDTRUNDEN = 5;
    private static final int DOWNLOADTAG = 18;
    GameApplication gameApplication;
    Spinner spinner;
    Spinner difficultySpinner;
    EditText rundenAnzahl;
    EditText rundenLaenge;
    List<String> auswahlmoeglichkeitenListe;
    ArrayAdapter<String> auswahlAdapter;
    ArrayAdapter<String> difficultyAuswahlAdapter;
    boolean keineAuswahl = true;
    int indexDownloaded = 0;
    boolean downloaded;
    //Diese Methode wird beim Start ausgeführt
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameApplication = (GameApplication) getApplication();
        gameApplication.setGame(new Game());
        gameApplication.getGame().addListener(this);
        spinner = findViewById(R.id.spinner);
        difficultySpinner = findViewById(R.id.spinnerDifficulty);
        rundenAnzahl = findViewById(R.id.rundenanzahlField);
        rundenLaenge = findViewById(R.id.rundenlaengeField);
        createDiffucultySpinner();
        auswahlmoeglichkeitenListe = new ArrayList<>();
        auswahlAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout
                .support_simple_spinner_dropdown_item, auswahlmoeglichkeitenListe);
        spinner.setAdapter(auswahlAdapter);

        //wenn im Spinner etwas neues ausgewählt wird, wird hier damit umgegangen
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                keineAuswahl = position == 0;
                if (keineAuswahl) {
                    rundenAnzahl.setHint("0");
                } else {
                    downloaded = position > indexDownloaded;
                    if (downloaded) {
                        gameApplication.getGame().getAlbum().setName((spinner.getSelectedItem().toString())
                                .substring(0, (spinner.getSelectedItem().toString()).length() - DOWNLOADTAG));
                    } else {
                        gameApplication.getGame().getAlbum().setName((spinner.getSelectedItem().toString()));
                    }
                    if (getImagesCount(gameApplication.getGame().getAlbum().getName()) < SDTRUNDEN) {
                        rundenAnzahl.setHint("" + getImagesCount(gameApplication.getGame().getAlbum().getName()));
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

        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        gameApplication.getGame().setDifficulty(new Punkte(20, 20000));
                        break;
                    case 2:
                        gameApplication.getGame().setDifficulty(new Punkte(5, 5000));
                        break;
                    default:
                        gameApplication.getGame().setDifficulty(new Punkte());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                gameApplication.getGame().setDifficulty(new Punkte());
            }
        });

        Button startButton = findViewById(R.id.startButton);
        //Wenn der Start-Button gedrückt wird wird entweder eine Fehlernachricht ausgegeben oder das Spiel gestartet
        startButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (keineAuswahl) {
                    Toast.makeText(getApplicationContext(), "Du hast keinen Ordner ausgewählt!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    boolean validL = false;
                    boolean validR = false;
                    if (getImagesCount(gameApplication.getGame().getAlbum().getName()) == 0) {
                        Toast.makeText(getApplicationContext(), "Das Album besitzt keine Bilder", Toast
                                .LENGTH_SHORT).show();
                    } else {
                    if (rundenAnzahl.getText().toString().equals("")) {
                        gameApplication.getGame().getRundenZaehler().setMaxRunde(Math.min(getImagesCount(
                                gameApplication.getGame().getAlbum().getName()), SDTRUNDEN));
                        validR = true;
                        } else {
                        int wantedRound = Integer.parseInt(rundenAnzahl.getText().toString());
                        if (wantedRound < 1) {
                            Toast.makeText(getApplicationContext(), "Du musst min. eine Runde spielen!", Toast
                                    .LENGTH_SHORT).show();
                        } else if (wantedRound > getImagesCount(gameApplication.getGame().getAlbum().getName())) {
                            Toast.makeText(getApplicationContext(), "Rundenanzahl uebertrifft Bilder in Album!", Toast
                                    .LENGTH_SHORT).show();
                        } else {
                            gameApplication.getGame().getRundenZaehler().setMaxRunde(wantedRound);
                            validR = true;
                        }
                        }
                    }

                    if (rundenLaenge.getText().toString().equals("")) {
                        gameApplication.getGame().getRundenZaehler().setRundenlaenge(SDTLAENGE);
                        validL = true;
                    } else {
                        int wantedLength = Integer.parseInt(rundenLaenge.getText().toString());
                        if (wantedLength > MAXSEKUNDEN) {
                            Toast.makeText(getApplicationContext(), "Eine Runde kann maximal 60 Sekunden lang sein",
                                    Toast.LENGTH_SHORT).show();
                        } else if (wantedLength < MINSEKUNDEN) {
                            Toast.makeText(getApplicationContext(), "Eine Runde muss mindestens 10 Sekunden lang sein",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            gameApplication.getGame().getRundenZaehler().setRundenlaenge(wantedLength);
                            validL = true;
                        }
                    }
                    if (validL && validR) {
                        Intent intent = new Intent(MainActivity.this, SpielansichtActivity.class);
                        intent.putExtra("downloaded", downloaded);
                        startActivity(intent);
                    }
                }
            }
        });

        Button downloadAnsichtButton = findViewById(R.id.downloadViewButton);
        //Mit diesem Button gelangt man in die Downloadansicht
        downloadAnsichtButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                startActivity(intent);
            }
        });

        Button updateAnsichtButton = findViewById(R.id.updateViewButton);
        //Mit diesem Button wird der Spinner geupdated
        updateAnsichtButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                auswahlAdapter.clear();
                auswahlmoeglichkeitenListe.add("Kein Ordner ausgewaehlt");
                try {
                    if (getAssets().list("albums") != null && ReadExif.getNamenAssets("", MainActivity.this)
                            .length > 0) {
                        auswahlmoeglichkeitenListe.addAll(Arrays.asList(ReadExif.getNamenAssets("",
                                MainActivity.this)));
                        indexDownloaded = ReadExif.getNamenAssets("", MainActivity.this).length;
                    }
                } catch (IOException e) {
                    Snackbar.make(button, "Das lesen der vorinstalliereten Bilder hat nicht funktioniert!",
                            Snackbar.LENGTH_SHORT).show();
                }
                addDownloads();
                auswahlAdapter.notifyDataSetChanged();
            }
        });
        updateAnsichtButton.performClick();
        showAllImagesInAlbums();
    }
    private void createDiffucultySpinner() {
        String[] difficultyListe = {"Normal","Einfach","Schwer"};
        difficultyAuswahlAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout
                .support_simple_spinner_dropdown_item, difficultyListe);
        difficultySpinner.setAdapter(difficultyAuswahlAdapter);
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

    //gibt alle Bilder mit Exif-Daten der Alben in Assets-Ordner im Log aus
    private void showAllImagesInAlbums() {
        for (String folder : ReadExif.getNamenAssets("", this)) {
            for (String file : ReadExif.getNamenAssets(folder, this)) {
                if (Bild.isImage(file)) {
                    String path = folder + "/" + file;
                    Log.i("Foto-Pfad", "Im Ordner " + folder + " liegt das Foto: " + file);
                    ReadExif.logExifAssets(path, ExifInterface.TAG_GPS_LONGITUDE, MainActivity.this);
                    ReadExif.logExifAssets(path, ExifInterface.TAG_GPS_LATITUDE, MainActivity.this);
                    ReadExif.logExifAssets(path, ExifInterface.TAG_IMAGE_DESCRIPTION, MainActivity.this);
                }
            }
        }
    }
    //gibt anzahl der Bilder in einem Ordner wieder
    private int getImagesCount(String folder) {
        return ReadExif.getImagesCount(folder, downloaded, this);
    }
    // Wenn in einem Modell eine Sache verändert wird, wird hier gesagt, was passieren soll
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    }
    //Wenn die Activity neugeladen wird, sollen alle Werte in den Models zurückgesetzt werden
    @Override
    protected void onResume() {
        super.onResume();
        spinner.setSelection(0, false);
        difficultySpinner.setSelection(0, false);
    }
    //Wenn die Activity beeendet wird, müssen die Listener entfernt werden
    @Override
    protected void onDestroy() {
        gameApplication.getGame().removeListener(this);
        super.onDestroy();
    }
}
