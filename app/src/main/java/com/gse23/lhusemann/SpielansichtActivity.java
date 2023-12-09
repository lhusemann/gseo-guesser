package com.gse23.lhusemann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

/**
 * Das ist die SpielansichtActivity, die zuerst ausgeführt wird, wenn diese Activity gestartet wird.
 */
public class SpielansichtActivity extends AppCompatActivity {
    Album pics;
    Button nextButton;
    Button validButton;
    ImageView bild;
    EditText longitudeField;
    EditText latitudeField;
    TextView linkText;
    TextView distanzText;
    TextView aktuellerScore;
    MapView karte;
    Context context;
    IConfigurationProvider provider;
    int anzahlBilder;
    int imageCount;
    String folder;
    String album = "albums";
    String noGuess;
    String noDistance;
    String komma = ",";
    String normalColor = "#808080";
    String zero = "0";
    String hint = "Hinweis";
    String ok = "Ok";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);
        bild = findViewById(R.id.bild);
        nextButton = findViewById(R.id.nextButton);
        longitudeField = findViewById(R.id.longitudeField);
        latitudeField = findViewById(R.id.latitudeField);
        validButton = findViewById(R.id.validButton);
        linkText = findViewById(R.id.link);
        distanzText = findViewById(R.id.textSchaetzung);
        aktuellerScore = findViewById(R.id.aktuellerScore);
        karte = findViewById(R.id.mapView);
        context = getApplicationContext();
        provider = Configuration.getInstance();
        provider.setUserAgentValue(context.getPackageName());
        provider.load(context, PreferenceManager.getDefaultSharedPreferences(context));
        karte.setTileSource(TileSourceFactory.MAPNIK);
        karte.setMultiTouchControls(true);
        noGuess = "Kein Guess abgegeben.";
        noDistance = "keine Schaetzung abgegeben";
        folder = getIntent().getStringExtra("ausgewaehltesAlbum");
        anzahlBilder = getIntent().getIntExtra("anzahlBilder", 0);
        pics = new Album();
        imageCount = 0;
        Log.i("Auswahl", "Das Album " + folder + " wurde ausgewaehlt.");
        try {
            this.addBilderInAlbum();
            this.bilderLoggen();
        } catch (CorruptedExifDataException | NoImagesInAlbumException e) {
            return;
            //Ausgabe für Nutzer im catch der bilderLoggen() und getExifData()
        }
        pics.mix();
        //NextButton
        nextButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                //falls es noch ein naechstes Bild gibt
                if (imageCount < anzahlBilder) {
                    longitudeField.setText(null);
                    latitudeField.setText(null);
                    bild.setImageDrawable(makeDrawable(album + "/" + folder + "/"
                            + pics.getBild(imageCount).getName()));
                    pics.logExifNoDec(imageCount);
                    imageCount++;
                    linkText.setText(noGuess);
                    linkText.setTextColor(Color.parseColor(normalColor));
                    distanzText.setText(noDistance);
                    aktuellerScore.setText(zero);
                } else {
                    AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
                    error.setMessage("Das waren alle Bilder im Album " + folder
                                    + ". Möchtest du zur Startansicht zurueckkehren?")
                            .setPositiveButton("Startansicht", (dialog, id) -> {
                                    finish();
                            })
                                .setNeutralButton("Abbruch", (dialog, id) -> {})
                                .setTitle(hint);
                    AlertDialog alert = error.create();
                    alert.show();
                }
            }
        });
        //da ich direkt bei einem Bild starten möchte drücke ich die next Taste einmal manuell.
            nextButton.performClick();
        //validButton
        validButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (Formatierung.isValidLongitude(longitudeField.getText().toString())
                        && Formatierung.isValidLatitude(latitudeField.getText().toString())) {
                    String latOne = latitudeField.getText().toString();
                    String lonOne = longitudeField.getText().toString();
                    String latTwo = pics.getBild(imageCount).getLatitude().toString();
                    String lonTwo = pics.getBild(imageCount).getLongtitude().toString();
                    double distance = Distanz.getDistance((Double.parseDouble(latOne)),
                            pics.getBild(imageCount).getLatitude(), Double.parseDouble(lonOne),
                            pics.getBild(imageCount).getLongtitude());
                    Log.i("Distanz der Schaetzung", Formatierung.getUnit(distance));
                    String link = "https://www.openstreetmap.org/directions?engine=fossgis_valhalla_foot&route="
                            + latOne + komma + lonOne + ";" + latTwo + komma + lonTwo;
                    Log.i("OpenStreetMaps-Link zur Schätzung", link);
                    linkText.setText(link);
                    linkText.setTextColor(Color.parseColor("#0000EE"));
                    distanzText.setText(Formatierung.getUnit(distance));
                    Punkte punktzahl = new Punkte(distance);
                    aktuellerScore.setText("" + punktzahl.getPunkte());
                } else {
                    linkText.setText(noGuess);
                    linkText.setTextColor(Color.parseColor(normalColor));
                    distanzText.setText(noDistance);
                    aktuellerScore.setText(zero);
                    AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
                    error.setMessage("Die eingegebenen Werte sind nicht Valide! Der Längengrad darf nur zwischen"
                                    + "-180 und +180, und der Breitengrad zwischen -90 und +90 liegen!")
                            .setPositiveButton(ok, (dialog, id) -> {})
                            .setTitle("Error");
                    AlertDialog alert = error.create();
                    alert.show();
                }
            }
        });
        //link
        linkText.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View link) {
                if (!linkText.getText().equals(noGuess)) {
                    linkText.setTextColor(Color.parseColor("#551A8B"));
                    openLink(linkText.getText().toString());
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        karte.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        karte.onPause();
    }
    //Zurücktaste
    @Override
    public void onBackPressed() {
        AlertDialog.Builder back = new AlertDialog.Builder(SpielansichtActivity.this);
        back.setMessage("Möchtest du das aktuelle Spiel wirklich beeenden?")
                .setPositiveButton("Ja", (dialog, id) -> {
                    finish();
                })
                .setNegativeButton("Nein", (dialog, id) -> {})
                .setTitle("Zurück");
        AlertDialog alert = back.create();
        alert.show();
    }
    //erstellt das Album aus allen Bilder des Ordners
    private void addBilderInAlbum() throws CorruptedExifDataException {
        String[] paths = this.getPath();
        for (int i = 0; i < paths.length; i++) {
            Bild aktuellesBild = new Bild(getNames()[i], getExifdata(paths[i], ExifInterface.TAG_IMAGE_DESCRIPTION),
                    getExifdata(paths[i], ExifInterface.TAG_GPS_LONGITUDE),
                    getExifdata(paths[i], ExifInterface.TAG_GPS_LATITUDE));
            pics.addBild(aktuellesBild);
        }
    }
    //gibt Path aller Bilder in einem Array wieder
    private String[] getPath() {
        String[] paths = new String[anzahlBilder];
        int counter = 0;
        String vorPath = album + "/" + folder;
        for (String file : getFolderInhalt(vorPath)) {
            if (Bild.isImage(file)) {
                paths[counter] = vorPath + "/" + file;
                counter++;
            }
        }
        return paths;
    }
    //gibt Namen aller Bilder in einem Array wieder
    private String[] getNames() {
        String[] paths = new String[anzahlBilder];
        int counter = 0;
        for (String file : getFolderInhalt(album + "/" + folder)) {
            if (Bild.isImage(file)) {
                paths[counter] = file;
                counter++;
            }
        }
        return paths;
    }
    //gibt Inhalt eines Ordner wieder ohne Fehlermeldung
    private String[] getFolderInhalt(String folder) {
        try {
            return getAssets().list(folder);
        } catch (IOException e) {
            return new String[0];
        }
    }
    //gibt Exifdaten zurück mit Hilfe der Klasse ReadExif
    private String getExifdata(String filepath, String exifTag) {
        try (InputStream in =  getAssets().open(filepath)) {
            String exifData = ReadExif.getData(exifTag, in);
            if (ReadExif.corrupted(exifData, exifTag)) {
                throw new CorruptedExifDataException(exifTag, filepath);
            } else {
                return exifData;
                }
        } catch (CorruptedExifDataException | IOException e) {
            AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
            error.setMessage("Eine und mehrere Dateien haben keine Standortdaten. Kehre auf Startansicht zurück.")
                    .setPositiveButton(ok, (dialog, id) -> {
                        this.finish();
                    })
                    .setTitle(hint);
            AlertDialog alert = error.create();
            alert.show();
            return "";
        }
    }
    //gibt alle Werte von allen Bildern aus
    private void bilderLoggen() throws NoImagesInAlbumException {
        try {
            if (pics.isEmptyUndLogAll()) {
                throw new NoImagesInAlbumException(album);
            }
        } catch (NoImagesInAlbumException e) {
            AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
            error.setMessage("Keine Bilder im ausgewaehltem Album vorhanden. Kehre auf Startansicht zurück")
                    .setPositiveButton(ok, (dialog, id) -> {
                        this.finish();
                    })
                    .setTitle(hint);
            AlertDialog alert = error.create();
            alert.show();
        }
    }
    //macht ein File zu Drawable
    private Drawable makeDrawable(String filepath) {
        Drawable erschaffen;
        try (InputStream in =  getAssets().open(filepath)) {
            erschaffen = Drawable.createFromStream(in, "src");
        } catch (IOException e) {
            return null;
        }
        return erschaffen;
    }
    //startet URL, ich habe dies Methode im Internet gefunden: https://www.youtube.com/watch?v=TU-yDCgpbZw
    private void openLink(String link) {
        Uri uri = Uri.parse(link);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
