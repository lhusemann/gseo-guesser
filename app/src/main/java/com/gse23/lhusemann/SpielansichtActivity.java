package com.gse23.lhusemann;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
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
 * Dies ist die Spielansicht, in der das Spiel gespielt wird
 */
public class SpielansichtActivity extends AppCompatActivity implements PropertyChangeListener {
    private static final String NOGUESS = "Kein Guess abgegeben.";
    private static final String KOMMA = ",";
    private static final int LINEBREITE = 20;
    private static final double WELTBREITE = 85.0;
    private static final double WELTLAENGE = 180.0;
    private static final float ZOOMWELT = 1.5f;

    GameApplication gameApplication;
    Button nextButton;
    Button validButton;
    Button aufgebenButton;
    ImageView bild;
    TextView longitudeField;
    TextView latitudeField;
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
    //Diese Methode wird beim Start ausgeführt
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);
        gameApplication = (GameApplication) getApplication();
        gameApplication.setMarkerKoordinaten(new MarkerKoordinaten(0, 0));
        gameApplication.getMarkerKoordinaten().addListener(this);
        gameApplication.getRundenZaehler().addListener(this);
        gameApplication.getAlbum().addListener(this);
        gameApplication.setGesamtscore(new Gesamtscore(0));
        gameApplication.getGesamtscore().addListener(this);
        gameApplication.setPunkte(new Punkte(0));
        gameApplication.getPunkte().addListener(this);
        gameApplication.setDistanz(new Distanz(0.0));
        gameApplication.getDistanz().addListener(this);
        gameApplication.setGame(new Game());
        gameApplication.getGame().addListener(this);
        gameApplication.setTimer(new RoundTimer(gameApplication.getRundenZaehler().getRundenlaenge()));
        gameApplication.getTimer().addListener(this);
        bild = findViewById(R.id.bild);
        nextButton = findViewById(R.id.nextButton);
        aufgebenButton = findViewById(R.id.ffButton);
        longitudeField = findViewById(R.id.longitudeField);
        latitudeField = findViewById(R.id.latitudeField);
        validButton = findViewById(R.id.validButton);
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
        paint.setStrokeWidth(LINEBREITE);
        nutzerMarker = new Marker(karte);
        nutzerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        nutzerMarker.setTitle("Ihre Eingabe");
        markerCorrect = new Marker(karte);
        Log.i("Auswahl", "Das Album " + gameApplication.getAlbum().getName() + " wurde ausgewaehlt.");
        try {
            this.addBilderInAlbum();
            gameApplication.getAlbum().logAll();
        } catch (CorruptedExifDataException e) {
            return;
            //-------------------------------------------------------------------------------------------------------
        }
        gameApplication.getAlbum().mix();
        //Dies soll passieren, wenn auf den Next-Button gedrückt wird
        nextButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                gameApplication.getRundenZaehler().incRunde(gameApplication.getAlbum().getSize());
                gameApplication.getPunkte().safePunkte();
                gameApplication.getPunkte().reset();
                gameApplication.getDistanz().resetDistance();
            }
        });
        // folgendes passiert bei clicken auf die Map
        karte.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint clickedP) {
                gameApplication.getGame().mapClick(clickedP.getLatitude(), clickedP.getLongitude());
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint clickedP) {
                return false;
            }
        }));
        //Das soll passieren wenn wir auf Bestätigen drücken
        validButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                gameApplication.getGame().validClick(gameApplication.getMarkerKoordinaten().getLongitude(),
                        gameApplication.getMarkerKoordinaten().getLatitude());
            }
        });
        Button showButton = findViewById(R.id.showButton);
        //Das soll passieren, wenn wir auf zentrieren drücken
        showButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                    IMapController mapController = karte.getController();
                    mapController.setCenter(nutzerMarker.getPosition());
            }
        });
        //Das soll passieren, wenn wir auf Aufgeben drücken
        aufgebenButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                AlertDialog.Builder back = new AlertDialog.Builder(SpielansichtActivity.this);
                back.setMessage("Möchtest du das aktuelle Spiel wirklich beeenden?")
                        .setPositiveButton("Ja", (dialog, id) -> {
                            gameApplication.getTimer().stopTimer();
                            finish();
                        })
                        .setNegativeButton("Abbruch", (dialog, id) -> {})
                        .setTitle("Aufgeben");
                AlertDialog alert = back.create();
                alert.show();
            }
        });
    //Das soll passieren, wenn wir auf Link drücken
        linkText.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View link) {
                gameApplication.getGame().linkClick(linkText.getText().toString());
            }
        });
        start();
    }
    //Wenn die Activity neugeladen wird, soll die Karte noch funktionieren
    @Override
    protected void onResume() {
        super.onResume();
        karte.onResume();
    }
    //Wenn die Activity neu aufgerufen wird durch einen neustart, sollen die Models zurückgesetzt werden
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        gameApplication.getGesamtscore().setGesamtscore(0);
        gameApplication.getPunkte().reset();
        gameApplication.getPunkte().resetScores();
        gameApplication.getMarkerKoordinaten().setLongitude(0);
        gameApplication.getMarkerKoordinaten().setLatitude(0);
        gameApplication.getDistanz().resetDistance();
        start();
    }
    //Wenn die Activity pausiert wird, soll die Karte pausiert werden
    @Override
    protected void onPause() {
        super.onPause();
        karte.onPause();
    }
    //Dies soll passieren wenn man die Von Android eingebaute zurücktaste verwendet. (Das selbe wie bei Aufgeben)
    @Override
    public void onBackPressed() {
        aufgebenButton.performClick();
    }
    //erstellt das Album aus allen Bilder des Ordners
    private void addBilderInAlbum() throws CorruptedExifDataException {
        gameApplication.getGame().checkBilder(gameApplication.getAlbum().isDownloaded(),
                gameApplication.getAlbum().getName());
    }
    //startet URL, ich habe dies Methode im Internet gefunden: https://www.youtube.com/watch?v=TU-yDCgpbZw
    private void openLink(String link) {
        Uri uri = Uri.parse(link);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
    //gibt ein Array von Ordnern/Dateien an bestimmer Stelle aus
    private String[] getAssetsInt(String folder) {
        try {
            return getAssets().list("albums/" + folder);
        } catch (IOException e) {
            return new String[0];
        }
    }
    //gibt Exifdaten zurück mit Hilfe der Klasse ReadExif
    public void addBildD(String file) {
        try {
            ExifInterface exifInterface = new ExifInterface(getFilesDir() + "/Album/" + gameApplication.getAlbum().getName() + "/" + file);
            addBild(exifInterface, file);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Fehler beim Lesen eines Bildes!", Toast.LENGTH_SHORT).show();
            finish();
        }
        }
    public void addBildI(String file) {
        try (InputStream in = getAssets().open("albums/" + gameApplication.getAlbum().getName() + "/" + file)) {
            ExifInterface exifInterface = new ExifInterface(in);
            addBild(exifInterface, file);
        } catch (IOException e) {
            Toast.makeText(this, "Fehler beim Lesen eines Bildes!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    public void addBild(ExifInterface exifInterface, String file) {
        String description = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        float[] koords = new float[2];
        try {
            double latitude;
            double longitude;
            if (exifInterface.getLatLong(koords)) {
                latitude = (double) koords[0];
                longitude = (double) koords[1];
            } else {
                throw new CorruptedExifDataException();
            }
            gameApplication.getAlbum().addBild(new Bild(file, description, longitude, latitude));
        } catch (CorruptedExifDataException e) {
            Log.i("CorruptedExifDataException", "Koordinaten konnten in der Datei " + file + " nicht gefunden werden.");
            Toast.makeText(this, "Koordinaten konnten in der Datei " + file + " nicht gefunden werden.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    public void start() {
        gameApplication.getRundenZaehler().incRunde(gameApplication.getAlbum().getSize());
        gameApplication.getPunkte().reset();
        gameApplication.getDistanz().resetDistance();
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
        if (changeName.equals("BildInc")) {
            beschreibung.setText(gameApplication.getAlbum().getBild((int) propertyChangeEvent.getNewValue())
                    .getDescription());
            bild.setImageDrawable(gameApplication.getAlbum().getBild((int) propertyChangeEvent.getNewValue())
                    .getDrawable(gameApplication.getAlbum().getName(), SpielansichtActivity.this, gameApplication
                            .getAlbum().isDownloaded()));
            gameApplication.getAlbum().logExifNoDec((int) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("RundenInc")) {
            gameApplication.getTimer().stopTimer();
            longitudeField.setText(null);
            latitudeField.setText(null);
            runde.setText("" + (int) propertyChangeEvent.getNewValue());
            linkText.setText(NOGUESS);
            linkText.setTextColor(Color.parseColor("#808080"));
            karte.getOverlays().remove(markerCorrect);
            karte.getOverlays().remove(nutzerMarker);
            karte.getOverlays().remove(line);
            List<GeoPoint> weltKarte = new ArrayList<>();
            weltKarte.add(new GeoPoint(WELTBREITE, WELTLAENGE));
            weltKarte.add(new GeoPoint(-WELTBREITE, -WELTLAENGE));
            karte.post(() -> {
                karte.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(weltKarte), false);
            });
            //gameApplication.getTimer().startTimer();----------------------------------------------------------------------
            gameApplication.getGame().rundeVorbei(false);
        }
        if (changeName.equals("goEndScreen")) {
        Intent intent = new Intent(SpielansichtActivity.this, EndActivity.class);
        startActivity(intent);
        }
        if (changeName.equals("Punkte")) {
            aktuellerScore.setText("" + ((int) propertyChangeEvent.getNewValue()));
        }
        if (changeName.equals("IncGesamtscore")) {
            gesScore.setText("" + (int) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Berechnung")) {
            aktuellerScore.setText("" + (int) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("DistanzCalc")) {
            Log.i("Distanz der Schaetzung", (String) propertyChangeEvent.getNewValue());
            String link = "https://www.openstreetmap.org/directions?engine=fossgis_valhalla_foot&route="
                    + gameApplication.getMarkerKoordinaten().getLatitude() + KOMMA + gameApplication
                    .getMarkerKoordinaten().getLongitude() + ";" + gameApplication.getAlbum().getBild(gameApplication
                    .getRundenZaehler().getRunde() - 1).getLatitude() + KOMMA + gameApplication.getAlbum()
                    .getBild(gameApplication.getRundenZaehler().getRunde() - 1).getLongtitude();
            Log.i("OpenStreetMaps-Link zur Schätzung", link);
            linkText.setText(link);
            linkText.setTextColor(Color.parseColor("#0000EE"));
            distanzText.setText((String) propertyChangeEvent.getNewValue());

            List<GeoPoint> geoPoints = new ArrayList<>();
            geoPoints.add(new GeoPoint(gameApplication.getMarkerKoordinaten().getLatitude(), gameApplication
                    .getMarkerKoordinaten().getLongitude()));
            geoPoints.add(new GeoPoint(gameApplication.getAlbum().getBild(gameApplication.getRundenZaehler()
                    .getRunde() - 1).getLatitude(), gameApplication.getAlbum().getBild(gameApplication
                            .getRundenZaehler().getRunde() - 1).getLongtitude()));
            nutzerMarker.setPosition(geoPoints.get(0));
            karte.getOverlays().remove(nutzerMarker);
            karte.getOverlays().add(nutzerMarker);
            markerCorrect.setPosition(geoPoints.get(1));
            markerCorrect.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            markerCorrect.setTitle("gesuchter Ort");
            karte.getOverlays().add(markerCorrect);
            line.setPoints(geoPoints);
            karte.getOverlays().add(line);
            karte.post(() -> {
                karte.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(geoPoints).increaseByScale(ZOOMWELT), true);
            });
        }
        if (changeName.equals("resetDistanz")) {
            distanzText.setText("keine Schaetzung abgegeben");
        }
        if (changeName.equals("correctLatitude")) {
            gameApplication.getMarkerKoordinaten().setLatitude((double) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("correctLongitude")) {
            gameApplication.getMarkerKoordinaten().setLongitude((double) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("NewMarkCheck")) {
            karte.getOverlays().remove(nutzerMarker);
            karte.getOverlays().add(nutzerMarker);
            karte.invalidate();
        }
        if (changeName.equals("log")) {
            Log.i("Hinweis",(String) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("snackbar")) {
            View currentView = getCurrentFocus();
            if (currentView != null) {
                Snackbar.make(currentView, (String) propertyChangeEvent.getNewValue(), Snackbar.LENGTH_SHORT).show();
            }
        }
        if (changeName.equals("ChangeTimer")) {
            timerText.setText("" +  (int) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("ZeitVorbei")) {
            gameApplication.getGame().zeitAbgelaufen();
            validButton.performClick();
        }
        if (changeName.equals("valid")) {
            gameApplication.getTimer().stopTimer();
            gameApplication.getDistanz().calcDistance(gameApplication.getMarkerKoordinaten()
                    .getLatitude(), gameApplication.getAlbum().getBild(gameApplication.getRundenZaehler()
                    .getRunde() - 1).getLatitude(), gameApplication.getMarkerKoordinaten()
                    .getLongitude(), gameApplication.getAlbum().getBild(gameApplication
                    .getRundenZaehler().getRunde() - 1).getLongtitude());
            gameApplication.getPunkte().berechne(gameApplication.getDistanz().getDistance());
            gameApplication.getGesamtscore().incGesamtscore(gameApplication.getPunkte().getPunkte());
        }
        if (changeName.equals("linkClick")) {
            linkText.setTextColor(Color.parseColor((String) propertyChangeEvent.getNewValue()));
            openLink(linkText.getText().toString());
        }
        if (changeName.equals("isDownloaded")) {
            gameApplication.getGame().forAddBilderLocation(getFilesDir() + "/Album/" + propertyChangeEvent.getNewValue(), (String) propertyChangeEvent.getNewValue(), gameApplication.getAlbum().isDownloaded());
        }
        if (changeName.equals("isInternal")) {
            gameApplication.getGame().forAddBilderWithName(getAssetsInt((String) propertyChangeEvent.getNewValue()), (String) propertyChangeEvent.getNewValue(), gameApplication.getAlbum().isDownloaded());
        }
        if (changeName.equals("addBild")) {
            addBildI((String) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("addBildD")) {
            addBildD((String) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("noImagesException")) {
            Log.i("NoImagesInAlbumException", propertyChangeEvent.getNewValue() + " hat keine Bilddateien enthalten.");
            Toast.makeText(this, "Keine Bilder im ausgewaehltem Album vorhanden.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //Wenn die Activity beeendet wird, müssen die Listener entfernt werden
    @Override
    protected void onDestroy() {
        gameApplication.getMarkerKoordinaten().removeListener(this);
        gameApplication.getRundenZaehler().removeListener(this);
        gameApplication.getGesamtscore().removeListener(this);
        gameApplication.getPunkte().removeListener(this);
        gameApplication.getAlbum().removeListener(this);
        gameApplication.getDistanz().removeListener(this);
        gameApplication.getGame().removeListener(this);
        gameApplication.getTimer().removeListener(this);
        gameApplication.getTimer().stopTimer();
        super.onDestroy();
    }
}
