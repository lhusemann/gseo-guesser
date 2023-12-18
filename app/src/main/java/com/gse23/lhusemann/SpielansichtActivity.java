package com.gse23.lhusemann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

/**
 * Dies ist die Spielansicht
 */
public class SpielansichtActivity extends AppCompatActivity implements PropertyChangeListener {
    GameApplication gameApplication;
    Button nextButton;
    Button validButton;
    Button showButton;
    Button aufgebenButton;
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
    Marker markerCorrect;
    Polyline line;
    Context context;
    IConfigurationProvider provider;
    String folder;
    String album = "albums";
    String noGuess;
    String noDistance;
    String komma = ",";
    String normalColor = "#808080";
    String hint = "Hinweis";
    String ok = "Ok";
    TimerTask timerTask;
    Timer timer;
    Handler timerHandler = new Handler();
    int counter;
    boolean rundeVorbei = false;
    boolean vielleichtKeinWertEinloggen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);
        gameApplication = (GameApplication) getApplication();
        gameApplication.setMarkerKoordinaten(new MarkerKoordinaten(0,0));
        gameApplication.getMarkerKoordinaten().addListener(this);
        gameApplication.getRundenZaehler().addListener(this);
        gameApplication.getAlbum().addListener(this);
        gameApplication.setGesamtscore(new Gesamtscore(0));
        gameApplication.getGesamtscore().addListener(this);
        gameApplication.setPunkte(new Punkte(0));
        gameApplication.getPunkte().addListener(this);
        bild = findViewById(R.id.bild);
        nextButton = findViewById(R.id.nextButton);
        aufgebenButton = findViewById(R.id.ffButton);
        longitudeField = findViewById(R.id.longitudeField);
        latitudeField = findViewById(R.id.latitudeField);
        validButton = findViewById(R.id.validButton);
        showButton = findViewById(R.id.showButton);
        linkText = findViewById(R.id.link);
        distanzText = findViewById(R.id.textSchaetzung);
        beschreibung = findViewById(R.id.beschreibungID);
        timerText = findViewById(R.id.timerID);
        aktuellerScore = findViewById(R.id.aktuellerScore);
        gesScore = findViewById(R.id.gesScore);
        runde = findViewById(R.id.rundenID);
        karte = findViewById(R.id.mapView);
        context = getApplicationContext();
        provider = Configuration.getInstance();
        provider.setUserAgentValue(context.getPackageName());
        provider.load(context, PreferenceManager.getDefaultSharedPreferences(context));
        karte.setTileSource(TileSourceFactory.MAPNIK);
        karte.setMultiTouchControls(true);
        line = new Polyline();
        Paint paint = line.getOutlinePaint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(20);
        nutzerMarker = new Marker(karte);
        markerCorrect = new Marker(karte);
        noGuess = "Kein Guess abgegeben.";
        noDistance = "keine Schaetzung abgegeben";
        folder = gameApplication.getAlbum().getName();
        Log.i("Auswahl", "Das Album " + folder + " wurde ausgewaehlt.");
        try {
            this.addBilderInAlbum();
            this.bilderLoggen();
        } catch (CorruptedExifDataException | NoImagesInAlbumException e) {
            return;
            //Exceptionbehandlung für Nutzer passiert im catch von bilderLoggen() und getExifData()
        }
        gameApplication.getAlbum().mix();
        //NextButton
        nextButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                gameApplication.getRundenZaehler().incRunde();

            }
        });
        // folgendes passiert bei clicken auf die Map:
        // koopiert von https://stackoverflow.com/questions/67850072/set-onclicklistener-for-mapview-using-open-street-map
        karte.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (!rundeVorbei) {
                    gameApplication.getMarkerKoordinaten().setLatitude(p.getLatitude());
                    gameApplication.getMarkerKoordinaten().setLongitude(p.getLongitude());
                    nutzerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    nutzerMarker.setTitle("Ihre Eingabe");
                    karte.getOverlays().add(nutzerMarker);
                }
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));
        // da ich direkt bei einem Bild starten möchte drücke ich die next Taste einmal manuell.
        nextButton.performClick();
        //validButton
        validButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (rundeVorbei) {
                    Snackbar.make(button, "Runde vorbei!" , Snackbar.LENGTH_SHORT).show();
                } else {
                    if (Formatierung.isValidLongitude(longitudeField.getText().toString())
                            && Formatierung.isValidLatitude(latitudeField.getText().toString())) {
                        stopTimer();
                        String latOne = latitudeField.getText().toString();
                        String lonOne = longitudeField.getText().toString();
                        String latTwo = gameApplication.getAlbum().getBild(gameApplication.getRundenZaehler().getRunde() - 1).getLatitude().toString();
                        String lonTwo = gameApplication.getAlbum().getBild(gameApplication.getRundenZaehler().getRunde() - 1).getLongtitude().toString();
                        gameApplication.getMarkerKoordinaten().setLongitude(Double.parseDouble(lonOne));
                        gameApplication.getMarkerKoordinaten().setLatitude(Double.parseDouble(latOne));
                        double distance = Distanz.getDistance(gameApplication.getMarkerKoordinaten().getLatitude(),
                                gameApplication.getAlbum().getBild(gameApplication.getRundenZaehler().getRunde() - 1).getLatitude(), gameApplication.getMarkerKoordinaten().getLongitude(),
                                gameApplication.getAlbum().getBild(gameApplication.getRundenZaehler().getRunde() - 1).getLongtitude());
                        Log.i("Distanz der Schaetzung", Formatierung.getUnit(distance));
                        String link = "https://www.openstreetmap.org/directions?engine=fossgis_valhalla_foot&route="
                                + latOne + komma + lonOne + ";" + latTwo + komma + lonTwo;
                        Log.i("OpenStreetMaps-Link zur Schätzung", link);
                        linkText.setText(link);
                        linkText.setTextColor(Color.parseColor("#0000EE"));
                        distanzText.setText(Formatierung.getUnit(distance));
                        gameApplication.getPunkte().berechne(distance);
                    } else {
                        if (!vielleichtKeinWertEinloggen) {
                            AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
                            error.setMessage("Die eingegebenen Werte sind nicht Valide! Der Längengrad darf nur zwischen"
                                            + "-180 und +180, und der Breitengrad zwischen -90 und +90 liegen!")
                                    .setPositiveButton(ok, (dialog, id) -> {
                                    })
                                    .setTitle("Error");
                            AlertDialog alert = error.create();
                            alert.show();
                        }
                    }
                }
            }
        });
        //center/show Button
        showButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                //zeige markerkoords setzte marker maybe wiel da gezeigt wird und center halt
                if (!latitudeField.getText().toString().equals("") || !longitudeField.getText().toString().equals("")) {
                    double lati = Double.parseDouble(latitudeField.getText().toString());
                    double longi = Double.parseDouble(longitudeField.getText().toString());
                    IMapController mapController = karte.getController();
                    nutzerMarker.setPosition(new GeoPoint(lati, longi));
                    mapController.setCenter(nutzerMarker.getPosition());
                }
            }
        });
        //AufgebenButton
        aufgebenButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                AlertDialog.Builder back = new AlertDialog.Builder(SpielansichtActivity.this);
                back.setMessage("Möchtest du das aktuelle Spiel wirklich beeenden?")
                        .setPositiveButton("Ja", (dialog, id) -> {
                            gameApplication.getAlbum().reset();
                            gameApplication.getRundenZaehler().setRunde(0);
                            gameApplication.getRundenZaehler().setRundenlaenge(0);
                            gameApplication.getRundenZaehler().setMaxRunde(0);
                            gameApplication.getAlbum().reset();
                            stopTimer();
                            finish();
                        })
                        .setNegativeButton("Abbruch", (dialog, id) -> {})
                        .setTitle("Aufgeben");
                AlertDialog alert = back.create();
                alert.show();
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        gameApplication.setPunkte(new Punkte(0));
        gameApplication.setMarkerKoordinaten(new MarkerKoordinaten(0,0));
        //---------------------------------------------------------------------------------------musst du noch testen listener noch aktiv ist
        nextButton.performClick();
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
                    gameApplication.getAlbum().reset();
                    gameApplication.getRundenZaehler().setRunde(0);
                    gameApplication.getRundenZaehler().setRundenlaenge(0);
                    gameApplication.getRundenZaehler().setMaxRunde(0);
                    stopTimer();
                    finish();
                })
                .setNegativeButton("Nein", (dialog, id) -> {})
                .setTitle("Zurück");
        AlertDialog alert = back.create();
        alert.show();
    }
    //erstellt das Album aus allen Bilder des Ordners
    private void addBilderInAlbum() throws CorruptedExifDataException {
        String[] paths = this.getPath().toArray(new String[0]);
        for (int i = 0; i < paths.length; i++) {
            Log.i("test","neuesBild");
            Bild aktuellesBild = new Bild(getNames()[i], getExifdata(paths[i], ExifInterface.TAG_IMAGE_DESCRIPTION),
                    getExifdata(paths[i], ExifInterface.TAG_GPS_LONGITUDE),
                    getExifdata(paths[i], ExifInterface.TAG_GPS_LATITUDE));
            gameApplication.getAlbum().addBild(aktuellesBild);
        }
    }
    //gibt Path aller Bilder in einem Array wieder
    private ArrayList<String> getPath() {
        ArrayList<String> paths = new ArrayList<>();
        String vorPath = album + "/" + folder;
        Log.i("test","bild suchen starten");
        for (String file : getFolderInhalt(vorPath)) {
            if (Bild.isImage(file)) {
                Log.i("test","bild gefunden");
                paths.add(vorPath + "/" + file);
            }
        }
        return paths;
    }
    //gibt Namen aller Bilder in einem Array wieder
    private String[] getNames() {
        ArrayList<String> paths = new ArrayList<>();
        for (String file : getFolderInhalt(album + "/" + folder)) {
            if (Bild.isImage(file)) {
                paths.add(file);
            }
        }
        return paths.toArray(new String[0]);
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
            if (gameApplication.getAlbum().isEmptyUndLogAll()) {
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
    //KANNST DU IN EIGENE KLASSE MIT PROPERTYCHANGE MACHEN!!!!!!!!
    //koopiert von https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    private void startTimer(){
        timer = new Timer();
        counter = gameApplication.getRundenZaehler().getRundenlaenge();
        timerTask = new TimerTask() {
            public void run() {
                timerHandler.post(new Runnable() {
                    public void run(){
                        timerText.setText("" + counter);
                        counter--;
                        if (counter<0) {
                            stopTimer();
                            Toast.makeText(SpielansichtActivity.this, "Die Zeit ist abgelaufen!", Toast.LENGTH_SHORT).show();
                            vielleichtKeinWertEinloggen = true;
                            validButton.performClick();
                            rundeVorbei = true;
                        }
                    }
                });
            }
        };

        timer.schedule(timerTask, 1, 1000);
    }
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String changeName = propertyChangeEvent.getPropertyName();
        if (changeName.equals("Laengengrad")) {
            nutzerMarker.setPosition(new GeoPoint(gameApplication.getMarkerKoordinaten().getLatitude(),
                    (double) propertyChangeEvent.getNewValue()));
            String wertAlsString = Double.toString((double) propertyChangeEvent.getNewValue());
            longitudeField.setText(wertAlsString);
            Log.i("Laengengrad des gesetzen Markers", wertAlsString);
        }
        if (changeName.equals("Breitengrad")) {
            nutzerMarker.setPosition(new GeoPoint((double) propertyChangeEvent.getNewValue(),
                    gameApplication.getMarkerKoordinaten().getLongitude()));
            String wertAlsString = Double.toString((double) propertyChangeEvent.getNewValue());
            latitudeField.setText(wertAlsString);
            Log.i("Breitengrad des gesetzen Markers", wertAlsString);
        }
        if (changeName.equals("RundenInc")) {
            stopTimer();

            if ((int) propertyChangeEvent.getNewValue() <= gameApplication.getRundenZaehler().getMaxRunde() && gameApplication.getAlbum().getSize() > 0) {
                longitudeField.setText(null);
                latitudeField.setText(null);
                beschreibung.setText(gameApplication.getAlbum().getBild((int) propertyChangeEvent.getNewValue()-1).getDescription());
                bild.setImageDrawable(makeDrawable(album + "/" + folder + "/"
                        + gameApplication.getAlbum().getBild((int) propertyChangeEvent.getNewValue()-1).getName()));
                gameApplication.getAlbum().logExifNoDec((int) propertyChangeEvent.getNewValue()-1);
                runde.setText("" + (int) propertyChangeEvent.getNewValue());
                linkText.setText(noGuess);
                linkText.setTextColor(Color.parseColor(normalColor));
                distanzText.setText(noDistance);
                gameApplication.getGesamtscore().incGesamtscore(gameApplication.getPunkte().getPunkte());
                gameApplication.getPunkte().safePunkte();
                gameApplication.getPunkte().reset();
                karte.getOverlays().remove(nutzerMarker);
                karte.getOverlays().remove(markerCorrect);
                karte.getOverlays().remove(line);
                List<GeoPoint> weltKarte = new ArrayList<>();
                weltKarte.add(new GeoPoint(85.0, 180.0));
                weltKarte.add(new GeoPoint(-85.0, -180.0));
                karte.post(() -> {
                    karte.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(weltKarte), false);
                });
                startTimer();
                rundeVorbei = false;
                vielleichtKeinWertEinloggen = false;
            } else {
                if (gameApplication.getAlbum().getSize() > 0) {
                    Intent intent = new Intent(SpielansichtActivity.this, EndActivity.class);
                    startActivity(intent);
                }
            }
        }
        if (changeName.equals("Punkte")) {
            aktuellerScore.setText("" + ((int) propertyChangeEvent.getNewValue()));
        }
        if (changeName.equals("IncGesamtscore")) {
            gesScore.setText("" + (int) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Berechnung")) {
            aktuellerScore.setText("" + (int) propertyChangeEvent.getNewValue());
            gameApplication.getGesamtscore().incGesamtscore((int) propertyChangeEvent.getNewValue());
            List<GeoPoint> geoPoints = new ArrayList<>();
            geoPoints.add(new GeoPoint(gameApplication.getMarkerKoordinaten().getLatitude(), gameApplication.getMarkerKoordinaten().getLongitude()));
            geoPoints.add(new GeoPoint(gameApplication.getAlbum().getBild(gameApplication.getRundenZaehler().getRunde()-1).
                    getLatitude(), gameApplication.getAlbum().getBild(gameApplication.getRundenZaehler().getRunde()-1).
                    getLongtitude()));
            nutzerMarker.setPosition(geoPoints.get(0));
            nutzerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            nutzerMarker.setTitle("Ihre Eingabe");
            karte.getOverlays().add(nutzerMarker);
            markerCorrect.setPosition(geoPoints.get(1));
            markerCorrect.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            markerCorrect.setTitle("gesuchter Ort");
            karte.getOverlays().add(markerCorrect);
            line.setPoints(geoPoints);
            karte.getOverlays().add(line);
            karte.post(() -> {
                karte.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(geoPoints), true);
            });
            rundeVorbei = true;
        }
    }

    @Override
    protected void onDestroy() {
        gameApplication.getMarkerKoordinaten().removeListener(this);
        gameApplication.getRundenZaehler().removeListener(this);
        gameApplication.getGesamtscore().removeListener(this);
        gameApplication.getPunkte().removeListener(this);
        gameApplication.getAlbum().removeListener(this);
        super.onDestroy();
    }
}
