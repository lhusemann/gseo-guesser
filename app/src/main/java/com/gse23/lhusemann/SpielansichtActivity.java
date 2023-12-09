package com.gse23.lhusemann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

/**
 * Dies ist die Spielansicht
 */
public class SpielansichtActivity extends AppCompatActivity implements PropertyChangeListener {
    GameApplication gameApplication;
    Album pics;
    Button nextButton;
    Button validButton;
    Button centerButton;
    ImageView bild;
    EditText longitudeField;
    EditText latitudeField;
    TextView linkText;
    TextView distanzText;
    TextView aktuellerScore;
    TextView gesScore;
    TextView runde;
    TextView beschreibung;
    TextView timerText;
    MapView karte;
    Marker nutzerMarker;
    Marker bildMarker;
    MarkerKoordinaten markerK;
    Context context;
    IConfigurationProvider provider;
    int anzahlBilder;
    int imageCount;
    String folder;
    Punkte currentpunkte;
    String album = "albums";
    String noGuess;
    String noDistance;
    String komma = ",";
    String normalColor = "#808080";
    String hint = "Hinweis";
    String ok = "Ok";
    int counter;

    private TimerTask timerTask;
    private Timer timer;
    private Handler timerHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);
        gameApplication = (GameApplication) getApplication();
        gameApplication.setMarkerKoordinaten(new MarkerKoordinaten(0,0));
        gameApplication.getMarkerKoordinaten().addListener(this);
        gameApplication.getRundenZaehler().addListener(this);
        gameApplication.getGesamtscore().addListener(this);
        bild = findViewById(R.id.bild);
        nextButton = findViewById(R.id.nextButton);
        longitudeField = findViewById(R.id.longitudeField);
        latitudeField = findViewById(R.id.latitudeField);
        validButton = findViewById(R.id.validButton);
        centerButton = findViewById(R.id.centerButton);
        linkText = findViewById(R.id.link);
        distanzText = findViewById(R.id.textSchaetzung);
        beschreibung = findViewById(R.id.beschreibungID);
        timerText = findViewById(R.id.timerID);
        aktuellerScore = findViewById(R.id.aktuellerScore);
        gesScore = findViewById(R.id.gesScore);
        gesScore.setText(gameApplication.getGesamtscore().getStringGesamtscore());
        runde = findViewById(R.id.rundenID);
        karte = findViewById(R.id.mapView);
        context = getApplicationContext();
        provider = Configuration.getInstance();
        provider.setUserAgentValue(context.getPackageName());
        provider.load(context, PreferenceManager.getDefaultSharedPreferences(context));
        karte.setTileSource(TileSourceFactory.MAPNIK);
        karte.setMultiTouchControls(true);
        nutzerMarker = new Marker(karte);
        noGuess = "Kein Guess abgegeben.";
        noDistance = "keine Schaetzung abgegeben";
        folder = getIntent().getStringExtra("ausgewaehltesAlbum");
        anzahlBilder = getIntent().getIntExtra("anzahlBilder", 0);
        pics = new Album();
        imageCount = 0;
        currentpunkte = new Punkte();
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
                    beschreibung.setText(pics.getBild(imageCount).getDescription());
                    bild.setImageDrawable(makeDrawable(album + "/" + folder + "/"
                            + pics.getBild(imageCount).getName()));
                    pics.logExifNoDec(imageCount);
                    imageCount++;
                    Rundenzaehler neueRunde = new Rundenzaehler(imageCount,1,30);//--------------------hier noch nicht richtig
                    gameApplication.setRundezaehler(neueRunde);
                    runde.setText("" + gameApplication.getRundenZaehler().getRunde());
                    linkText.setText(noGuess);
                    linkText.setTextColor(Color.parseColor(normalColor));
                    distanzText.setText(noDistance);
                    int scoreDavor = gameApplication.getGesamtscore().getGesamtscore();
                    Gesamtscore score = new Gesamtscore(currentpunkte.getPunkte() + scoreDavor);
                    gameApplication.setGesamtscore(score);
                    gesScore.setText(gameApplication.getGesamtscore().getStringGesamtscore());
                    currentpunkte.reset();
                    aktuellerScore.setText(currentpunkte.getStringPunkte());
                    karte.getOverlays().remove(nutzerMarker);
                    startTimer();
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
        // folgendes passiert bei clicken auf die Map:
        // koopiert von https://stackoverflow.com/questions/67850072/set-onclicklistener-for-mapview-using-open-street-map
        karte.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Log.i("Laengengrad des gesetzen Markers","" + p.getLatitude());
                Log.i("Breitengrad des gesetzen Markers","" + p.getLongitude());
                nutzerMarker.setPosition(new GeoPoint(p.getLatitude(), p.getLongitude()));
                nutzerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                nutzerMarker.setTitle("Ihre Eingabe");
                markerK = new MarkerKoordinaten(p.getLongitude(), p.getLatitude());
                gameApplication.setMarkerKoordinaten(markerK);
                karte.getOverlays().add(nutzerMarker);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));
        //validButton
        validButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                Log.i("",""+ karte.getLatitudeSpanDouble());
                Log.i("",""+ karte.getLongitudeSpanDouble());
                if (Formatierung.isValidLongitude(longitudeField.getText().toString())
                        && Formatierung.isValidLatitude(latitudeField.getText().toString())) {
                    String latOne = latitudeField.getText().toString();
                    String lonOne = longitudeField.getText().toString();
                    String latTwo = pics.getBild(imageCount-1).getLatitude().toString();
                    String lonTwo = pics.getBild(imageCount-1).getLongtitude().toString();
                    double distance = Distanz.getDistance((Double.parseDouble(latOne)),
                            pics.getBild(imageCount-1).getLatitude(), Double.parseDouble(lonOne),
                            pics.getBild(imageCount-1).getLongtitude());
                    Log.i("Distanz der Schaetzung", Formatierung.getUnit(distance));
                    String link = "https://www.openstreetmap.org/directions?engine=fossgis_valhalla_foot&route="
                            + latOne + komma + lonOne + ";" + latTwo + komma + lonTwo;
                    Log.i("OpenStreetMaps-Link zur Schätzung", link);
                    linkText.setText(link);
                    linkText.setTextColor(Color.parseColor("#0000EE"));
                    distanzText.setText(Formatierung.getUnit(distance));
                    currentpunkte.berechne(distance);
                    aktuellerScore.setText(currentpunkte.getStringPunkte());
                    gesScore.setText(gameApplication.getGesamtscore().getStringGesamtscore());


                } else {
                    linkText.setText(noGuess);
                    linkText.setTextColor(Color.parseColor(normalColor));
                    distanzText.setText(noDistance);
                    currentpunkte.reset();
                    aktuellerScore.setText(currentpunkte.getStringPunkte());
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
        //centerButton
        centerButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                IMapController mapController = karte.getController();
                mapController.setCenter(nutzerMarker.getPosition());
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
    //koopiert von https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    private void stopTimer(){
        if(timer != null){
            timer.cancel();
            timer.purge();
            counter = 0;
            timerText.setText("" + counter);
        }
    }
    //koopiert von https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    private void startTimer(){
        timer = new Timer();
        counter = 60;
        timerTask = new TimerTask() {
            public void run() {
                timerHandler.post(new Runnable() {
                    public void run(){
                        timerText.setText("" + counter);
                        counter--;
                        if (counter<1) {
                            stopTimer();
                            AlertDialog.Builder over = new AlertDialog.Builder(SpielansichtActivity.this);
                            over.setMessage("Deine Zeit ist Abgelaufen!")
                                    .setPositiveButton("Nächstes Bild", (dialog, id) -> {
                                        nextButton.performClick();
                                    })
                                    .setTitle("Zeit vorbei!");
                            AlertDialog alert = over.create();
                            alert.show();
                        }
                    }
                });
            }
        };

        timer.schedule(timerTask, 1, 1000);
    }
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
    @Override
    protected void onDestroy() {
        gameApplication.getMarkerKoordinaten().removeListener(this);
        gameApplication.getRundenZaehler().removeListener(this);
        gameApplication.getGesamtscore().removeListener(this);
        super.onDestroy();
    }
}
