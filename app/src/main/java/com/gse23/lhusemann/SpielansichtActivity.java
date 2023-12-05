package com.gse23.lhusemann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import java.io.IOException;
import java.io.InputStream;
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
/**
 * Das ist die SpielansichtActivity, die zuerst ausgeführt wird, wenn diese Activity gestartet wird.
 */
public class SpielansichtActivity extends AppCompatActivity {
    Bild[] bilder;
    Button nextButton;
    Button validButton;
    ImageView bild;
    EditText longitudeField;
    EditText latitudeField;
    TextView linkText;
    TextView distanzText;
    TextView aktuellerScore;
    int anzahlBilder;
    int imageCount;
    String folder;
    String album = "albums";
    String noGuess;
    String noDistance;
    String komma = ",";
    String normalColor = "#808080";
    String zero = "0";
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
        noGuess = "Kein Guess abgegeben.";
        noDistance = "keine Schaetzung abgegeben";
        folder = getIntent().getStringExtra("ausgewaehltesAlbum");
        anzahlBilder = getIntent().getIntExtra("anzahlBilder", 0);
        imageCount = 0;
        Log.i("Ausgewahl", "Das Album " + folder + " wurde ausgewaehlt.");
        try {
            bilder = makeImageArray();
            bilderAusgabe();
        } catch (CorruptedExifDataException | NoImagesInAlbumException e) {
            throw new RuntimeException(e);
        }
        mischenErstesBild();
        //NextButton
        nextButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                //falls es noch ein naechstes Bild gibt
                if (imageCount + 1 < anzahlBilder) {
                    longitudeField.setText(null);
                    latitudeField.setText(null);
                    bild.setImageDrawable(makeDrawable(album + "/" + folder + "/" + bilder[imageCount + 1].getName()));
                    Log.i("Name des angezeigten Bildes", bilder[imageCount + 1].getName());
                    Log.i("Laengengrad des angezeigten Bildes", "" + bilder[imageCount + 1].getLongtitude());
                    Log.i("Breitengrad des angezeigten Bildes", "" + bilder[imageCount + 1].getLatitude());
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
                                .setTitle("Hinweis");
                    AlertDialog alert = error.create();
                    alert.show();
                }
            }
        });
        //validButton
        validButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (isValidLongitude(longitudeField.getText().toString())
                        && isValidLatitude(latitudeField.getText().toString())) {
                    String latOne = latitudeField.getText().toString();
                    String lonOne = longitudeField.getText().toString();
                    String latTwo = bilder[imageCount].getLatitude().toString();
                    String lonTwo = bilder[imageCount].getLongtitude().toString();
                    double distance = Distanz.getDistance((Double.parseDouble(latOne)), bilder[imageCount].getLatitude(),
                            Double.parseDouble(lonOne), bilder[imageCount].getLongtitude());
                    Log.i("Distanz der Schaetzung", FormatUmrechner.getUnit(distance));
                    String link = "https://www.openstreetmap.org/directions?engine=fossgis_valhalla_foot&route="
                            + latOne + komma + lonOne + ";" + latTwo + komma + lonTwo;
                    Log.i("OpenStreetMaps-Link zur Schätzung", link);
                    linkText.setText(link);
                    linkText.setTextColor(Color.parseColor("#0000EE"));
                    distanzText.setText(FormatUmrechner.getUnit(distance));
                    aktuellerScore.setText("" + getPunkte(distance));
                } else {
                    linkText.setText(noGuess);
                    linkText.setTextColor(Color.parseColor(normalColor));
                    distanzText.setText(noDistance);
                    aktuellerScore.setText(zero);
                    AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
                    error.setMessage("Die eingegebenen Werte sind nicht Valide! Der Längengrad darf nur zwischen"
                                    + "-180 und +180, und der Breitengrad zwischen -90 und +90 liegen!")
                            .setPositiveButton("Ok", (dialog, id) -> {})
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
    //erstellt ein Array aus allen Bilder des Ordners
    private Bild[] makeImageArray() throws CorruptedExifDataException {
        Bild[] bilderArray = new Bild[anzahlBilder];
        String[] paths = this.getPath();
        for (int i = 0; i < anzahlBilder; i++) {
            Bild aktuellesBild = new Bild(getNames()[i], getExifdata(paths[i], ExifInterface.TAG_IMAGE_DESCRIPTION),
                    getExifdata(paths[i], ExifInterface.TAG_GPS_LONGITUDE),
                    getExifdata(paths[i], ExifInterface.TAG_GPS_LATITUDE));
            bilderArray[i] = aktuellesBild;
        }
        return bilderArray;
    }
    //gibt Path aller Bilder in einem Array wieder
    private String[] getPath() {
        String[] paths = new String[anzahlBilder];
        int counter = 0;
        String vorPath = album + "/" + folder;
        for (String file : getFolderInhalt(vorPath)) {
            if (this.isImage(file)) {
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
            if (this.isImage(file)) {
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
    //prüft ob es sich um eine Bild-Datei handelt
    private boolean isImage(String name) {
        return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
    }
    //gibt Exifdaten zurück
    private String getExifdata(String filepath, String exifTag) {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            if (exifInterface.getAttribute(exifTag) == null && !(exifTag.equals(ExifInterface.TAG_IMAGE_DESCRIPTION))) {
                    throw new CorruptedExifDataException(exifTag, filepath);
            } else {
                if (exifTag.equals(ExifInterface.TAG_IMAGE_DESCRIPTION)
                        && exifInterface.getAttribute(exifTag) == null) {
                    return "nicht vorhanden";
                } else {
                    return exifInterface.getAttribute(exifTag);
                }
            }
        } catch (CorruptedExifDataException | IOException e) {
            finish();
            return "";
        }
    }
    //gibt alle Werte von allen Bildern aus
    private void bilderAusgabe() throws NoImagesInAlbumException {
        try {
            if (anzahlBilder < 1) {
                throw new NoImagesInAlbumException(album);
            } else {
                for (Bild bild : bilder) {
                    Log.i("Bildname", bild.getName());
                    Log.i("Beschreibung", bild.getDescription());
                    Log.i("Laengengrad", "" + bild.getLongtitude());
                    Log.i("Breitengrad", "" + bild.getLatitude());
                }
            }
        } catch (NoImagesInAlbumException e) {
            this.finish();
        }
    }
    //mischt das Bilder-Array
    private void mischenErstesBild() {
        if (anzahlBilder > 0) {
            final int mischungen = 100;
            for (int i = 0; i < mischungen; i++) {
                int erstesBild = (int) (Math.random() * anzahlBilder - 1);
                int zweitesBild = (int) (Math.random() * anzahlBilder - 1);
                Bild platzhalter = bilder[erstesBild];
                bilder[erstesBild] = bilder[zweitesBild];
                bilder[zweitesBild] = platzhalter;
            }
            bild.setImageDrawable(makeDrawable(album + "/" + folder + "/" + bilder[imageCount].getName()));
            Log.i("Name des aktuellen Bildes", bilder[imageCount].getName());
            Log.i("Laengengrad des aktuellen Bildes", "" + bilder[imageCount].getLongtitude());
            Log.i("Breitengrad des aktuellen Bildes", "" + bilder[imageCount].getLatitude());
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
    //prüft ob valider Laengengrad
    private boolean isValidLongitude(String longitude) {
        final double min = -180.0;
        final double max = 180.0;
        try {
            return min <= Double.parseDouble(longitude) && Double.parseDouble(longitude) <= max;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    //prüft ob valider Breitengrad
    private boolean isValidLatitude(String latitude) {
        final double min = -90;
        final double max = 90;
        try {
            return min <= Double.parseDouble(latitude) && Double.parseDouble(latitude) <= max;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    //startet URL, ich habe dies Methode im Internet gefunden: https://www.youtube.com/watch?v=TU-yDCgpbZw
    private void openLink(String link) {
        Uri uri = Uri.parse(link);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
    //berechner Punktzahl
    private int getPunkte(double distanz) {
        final double minDistance = 10.0;
        final double maxDistance = 10000.0;
        final int maxPoints = 5000;
        if (distanz < minDistance) {
            return maxPoints;
        }
        if (distanz > maxDistance) {
            return 0;
        }
        return (int) ((int) (maxPoints / Math.log(maxDistance / minDistance)) * Math.log(maxDistance / distanz));
    }
}
