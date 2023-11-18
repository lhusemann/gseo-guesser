package com.gse23.lhusemann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Das ist die SpielansichtActivity, die zuerst ausgeführt wird, wenn diese Activity gestartet wird.
 */
public class SpielansichtActivity extends AppCompatActivity {
    String folder;
    int anzahlBilder;
    String album = "albums";
    Bild[] bilder;
    Button nextButton;
    ImageView bild;
    EditText longitudeEditText;
    EditText latitudeEditText;
    Button validButton;
    int imageCount;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);
        bild = findViewById(R.id.bild);
        nextButton = findViewById(R.id.nextButton);
        longitudeEditText = findViewById(R.id.longitudeEditText);
        latitudeEditText = findViewById(R.id.latitudeEditText);
        validButton = findViewById(R.id.validButton);
        folder = getIntent().getStringExtra("ausgewaehltesAlbum");
        anzahlBilder = getIntent().getIntExtra("anzahlBilder", 0);
        try {
            bilder = makeImageArray();
            bilderAusgabe();
            mischenErstesBild();
        } catch (CorruptedExifDataException | NoImagesInAlbumException e) {
            throw new RuntimeException(e);
        }
        imageCount = 1;
        //NextButton
        nextButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (imageCount < anzahlBilder) {
                    longitudeEditText.setText(null);
                    latitudeEditText.setText(null);
                    bild.setImageDrawable(makeDrawable(album + "/" + folder + "/" + bilder[imageCount].getName()));
                    Log.i("Name des angezeigten Bildes", bilder[imageCount].getName());
                    Log.i("Laengengrad des angezeigten Bildes", "" + bilder[imageCount].getLongtitude());
                    Log.i("Breitengrad des angezeigten Bildes", "" + bilder[imageCount].getLatitude());
                    imageCount++;
                } else {
                    AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
                    error.setMessage("Das waren alle Bilder im Album " + folder +
                                    ". Möchtest du zur Startansicht zurueckkehren?")
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
                if (isValidLongitude(longitudeEditText.getText().toString()) &&
                        isValidLatitude(latitudeEditText.getText().toString())) {
                    //ja
                } else {
                    AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
                    error.setMessage("Die eingegebenen Werte sind nicht Valide! Es dürfen nur Zahlen verwendet " +
                                    "werden! Zudem darf der Längengrad nur zwischen -180 und +180 und der " +
                                    "Breitengrad zwischen -90 und +90 liegen!")
                            .setPositiveButton("Ok", (dialog, id) -> {})
                            .setTitle("Error");
                    AlertDialog alert = error.create();
                    alert.show();
                }
            }
        });
    }
    //erstellt ein Array aus allen Bilder des Ordners
    private Bild[] makeImageArray() throws CorruptedExifDataException {
        Bild[] bilderArray = new Bild[anzahlBilder];
        String[] paths = this.getPath();
        for (int i = 0; i<anzahlBilder;i++) {
            Bild aktuellesBild = new Bild(getNames()[i], getExifdata(paths[i], ExifInterface.TAG_IMAGE_DESCRIPTION),
                    makeValue(getExifdata(paths[i], ExifInterface.TAG_GPS_LONGITUDE)),
                    makeValue(getExifdata(paths[i], ExifInterface.TAG_GPS_LATITUDE)));
            bilderArray[i] = aktuellesBild;
        }
        return bilderArray;
    }
    //macht die String zu verwendbaren Double
    private double helperMakeValue (String tag) {
        StringBuilder valueAsString = new StringBuilder();
        for (int i = 0; i<tag.length() && Character.isDigit(tag.charAt(i)); i++) {
            valueAsString.append(tag.charAt(i));
        }
        if (valueAsString.equals("")) {
            return 0;
        }
        return Integer.parseInt(valueAsString.toString());
    }
    //macht die String-Exifdaten zu dezimalen Graden
    private double makeValue (String tag) {
        String[] dms = tag.split(",");
        double degreeNumber = helperMakeValue(dms[0]);
        double minuteNumber = helperMakeValue(dms[1])/60;
        String[] sec = dms[2].split("/");
        double secondNumber = helperMakeValue(sec[0])/helperMakeValue(sec[1])/3600;
        return degreeNumber + minuteNumber + secondNumber;
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
                        return name.charAt(laenge - five) == '.';
                    }
                }
            }
        }
        return false;
    }
    //gibt Exifdaten zurück
    private String getExifdata(String filepath, String exifTag) throws CorruptedExifDataException {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            if (exifInterface.getAttribute(exifTag) == null) {
                throw new CorruptedExifDataException(exifTag, filepath);
            } else {
                return exifInterface.getAttribute(exifTag);
            }
        } catch (CorruptedExifDataException | IOException e) {
            finish();
            return "";
        }
    }
    //gibt alle Werte von allen Bildern aus
    private void bilderAusgabe() throws NoImagesInAlbumException {
        try {
            if(anzahlBilder<1) {
                throw new NoImagesInAlbumException(album);
            } else {
                for (Bild bild : bilder) {
                    Log.i("Bildname", bild.getName());
                    Log.i("Beschreibung", bild.getDescription());
                    Log.i("Laengengrad", "" + bild.getLongtitude());
                    Log.i("Breitengrad", "" + bild.getLatitude());
                }
            }
        }catch (NoImagesInAlbumException e){
            this.finish();
        }
    }
    //mischt das Bilder-Array
    private void mischenErstesBild() {
        if (anzahlBilder>0) {
            bild.setImageDrawable(makeDrawable(album + "/" + folder+"/"+bilder[0].getName()));
            Log.i("Name des angezeigten Bildes", bilder[imageCount].getName());
            Log.i("Laengengrad des angezeigten Bildes", "" + bilder[imageCount].getLongtitude());
            Log.i("Breitengrad des angezeigten Bildes", "" + bilder[imageCount].getLatitude());
            for (int i = 0; i < 100; i++) {
                int erstesBild = (int) (Math.random() * anzahlBilder - 1);
                int zweitesBild = (int) (Math.random() * anzahlBilder - 1);
                Bild platzhalter = bilder[erstesBild];
                bilder[erstesBild] = bilder[zweitesBild];
                bilder[zweitesBild] = platzhalter;
            }
        }
    }
    //macht ein File zu Drawable
    private Drawable makeDrawable(String filepath) {
        Drawable erschaffen;
        try (InputStream in =  getAssets().open(filepath)) {
            erschaffen = Drawable.createFromStream(in,"src");
        } catch (IOException e) {
            return null;
        }
        return erschaffen;
    }
    //prüft ob valider Laengengrad
    private boolean isValidLongitude(String longitude) {
        try {
            return -180.0 < Double.parseDouble(longitude) && Double.parseDouble(longitude) < 180.0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    //prüft ob valider Breitengrad
    private boolean isValidLatitude(String latitude) {
        try {
            return -90.0 < Double.parseDouble(latitude) && Double.parseDouble(latitude) < 90.0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}

