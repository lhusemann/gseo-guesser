package com.gse23.lhusemann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import java.io.IOException;
import java.io.InputStream;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    int imageCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);
        ImageView bild = findViewById(R.id.bild);
        folder = getIntent().getStringExtra("ausgewaehltesAlbum");
        anzahlBilder = getIntent().getIntExtra("anzahlBilder", 0);
        try {
            bilder = makeImageArray();
            bilderAusgabe();
        } catch (CorruptedExifDataException | NoImagesInAlbumException e) {
            throw new RuntimeException(e);
        }
        mischen();
        bild.setImageDrawable(makeDrawable(album + "/" + folder+"/"+bilder[0].getName()));
        imageCount = 1;
        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                Log.i("test",""+imageCount);
                Log.i("test",""+anzahlBilder);
                if(imageCount<anzahlBilder) {
                    bild.setImageDrawable(makeDrawable(album + "/" + folder+"/"+bilder[imageCount].getName()));
                    imageCount++;
                } else {
                    AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
                    error.setMessage("Das waren alle Bilder im Album " + folder +
                                    ". Möchtest du zur Startansicht zurueckkehren?")
                            .setPositiveButton("Startansicht", (dialog, id) -> {finish();})
                            .setNeutralButton("Abbruch", (dialog, id) -> {})
                            .setTitle("Hinweis");
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
    //macht die String-Exifdaten zu verwendbaren Double
    private double makeValue (String tag) {
        StringBuilder valueAsString = new StringBuilder();
        for (int i = 0; Character.isDigit(tag.charAt(i)); i++) {
            valueAsString.append(tag.charAt(i));
        }
        if (valueAsString.equals("")) {
            //Log.i("CorruptedExifDataException", "Die Datei im Verzeichnes" + path + " hat unvollständige Daten.");
            //finish();
            return 0;
        }
        return Integer.parseInt(valueAsString.toString());
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
    private String getExifdata(String filepath, String exifTag) throws CorruptedExifDataException {
        try (InputStream in =  getAssets().open(filepath)) {
            ExifInterface exifInterface = new ExifInterface(in);
            if (exifInterface.getAttribute(exifTag) == null || exifInterface.getAttribute(exifTag).equals("")) {
                throw new CorruptedExifDataException();
            } else {
                return exifInterface.getAttribute(exifTag);
            }
        } catch (CorruptedExifDataException | IOException e) {
            Log.i("CorruptedExifDataException",exifTag + " konnte in " + filepath +
                    " nicht gefunden werden");
            finish();
            return "";
        }
    }
    //gibt alle Werte von allen Bildern aus
    private void bilderAusgabe() throws NoImagesInAlbumException {
        try {
            for (Bild bild : bilder) {
                Log.i("Bildname", bild.getName());
                Log.i("Beschreibung", bild.getDescription());
                Log.i("Laengengrad", "" + bild.getLongtitude());
                Log.i("Breitengrad", "" + bild.getLatitude());
            }
            if(bilder.length==0) {
                throw new NoImagesInAlbumException();
            }
        }catch (NoImagesInAlbumException e){
            Log.i("NoImagesInAlbumException",folder + " hat keine Bilddateien enthalten. Du wirst auf die " +
                    "Startansicht zurück geworfen");
            finish();
        }
    }
    //mischt das Bilder-Array
    private void mischen() {
        for (int i = 0; i < 100; i++){
            int erstesBild = (int) (Math.random()*anzahlBilder-1);
            int zweitesBild = (int) (Math.random()*anzahlBilder-1);
            Bild platzhalter = bilder[erstesBild];
            bilder[erstesBild] = bilder[zweitesBild];
            bilder[zweitesBild] = platzhalter;
        }
    }
    private Drawable makeDrawable(String filepath) {
        Drawable erschaffen;
        try (InputStream in =  getAssets().open(filepath)) {
            erschaffen = Drawable.createFromStream(in,"src");
        } catch (IOException e) {
            return null;
        }
        return erschaffen;
    }
}

