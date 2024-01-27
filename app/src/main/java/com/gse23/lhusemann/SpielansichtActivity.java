package com.gse23.lhusemann;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
    boolean downloaded;
    //Diese Methode wird beim Start ausgeführt
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);
        gameApplication = (GameApplication) getApplication();
        gameApplication.getGame().addListener(this);
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
        downloaded = getIntent().getBooleanExtra("downloaded", false);
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
        nutzerMarker.setPosition(new GeoPoint(0.0,0.0));
        nutzerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        nutzerMarker.setTitle("Ihre Eingabe");
        karte.getOverlays().remove(nutzerMarker);
        markerCorrect = new Marker(karte);
        markerCorrect.setPosition(new GeoPoint(0.0,0.0));
        markerCorrect.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerCorrect.setTitle("gesuchter Ort");
        karte.getOverlays().remove(markerCorrect);
        Log.i("Auswahl", "Das Album " + gameApplication.getGame().getAlbum().getName() + " wurde ausgewaehlt.");
        try {
            this.addBilderInAlbum();
            gameApplication.getGame().start();
        } catch (CorruptedExifDataException e) {
            Log.i("CorruptedExifDataException", "Exifdaten in einer oder mehrerer Dateien nicht enthalten!");
            Toast.makeText(this, "Exifdaten in einer oder mehrerer Dateien nicht enthalten!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NoImagesInAlbumException e) {
            Log.i("NoImagesInAlbumException", "Das Album hat keine Bilddateien enthalten.");
            Toast.makeText(this, "Keine Bilder im ausgewaehltem Album vorhanden.", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IOException e) {
            Log.i("Fehler", "Fehler beim Lesen eines Bildes!");
            Toast.makeText(this, "Fehler beim Lesen eines Bildes!", Toast.LENGTH_SHORT).show();
            finish();
        }
        //Dies soll passieren, wenn auf den Next-Button gedrückt wird
        nextButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                    gameApplication.getGame().nextRound();
            }
        });

        // folgendes passiert bei clicken auf die Map
        karte.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint clickedP) {
                gameApplication.getGame().koordInput(clickedP.getLatitude(), clickedP.getLongitude());
                karte.invalidate();
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
                gameApplication.getGame().eingabeValidieren();
            }
        });

        Button showButton = findViewById(R.id.showButton);
        //Das soll passieren, wenn wir auf zentrieren drücken
        showButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (gameApplication.getGame().markerIsPlaced()) {
                    IMapController mapController = karte.getController();
                    mapController.setCenter(nutzerMarker.getPosition());
                }
            }
        });

        //Das soll passieren, wenn wir auf Aufgeben drücken
        aufgebenButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                AlertDialog.Builder back = new AlertDialog.Builder(SpielansichtActivity.this);
                back.setMessage("Möchtest du das aktuelle Spiel wirklich beeenden?")
                        .setPositiveButton("Ja", (dialog, id) -> {
                            gameApplication.getGame().aufgeben();
                            finish();
                        })
                        .setNegativeButton("Abbruch", (dialog, id) -> {})
                        .setTitle("Aufgeben");
                AlertDialog alert = back.create();
                alert.show();
            }
        });
    //Das soll passieren, wenn man auf den Link drückt
        linkText.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View link) {
                if (gameApplication.getGame().isRundeVorbeiWithMarker()) {
                   openLink(linkText.getText().toString());
                }
            }
        });
    }
    //Wenn die Activity neugeladen wird
    @Override
    protected void onResume() {
        super.onResume();
        karte.onResume();
    }
    //Wenn die Activity neu aufgerufen wird durch einen neustart, sollen die Rundenwerte im Model zurückgesetzt werden
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        gameApplication.getGame().restart();
    }
    //Wenn die Activity pausiert wird
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
    //startet URL, ich habe dies Methode im Internet gefunden: https://www.youtube.com/watch?v=TU-yDCgpbZw
    private void openLink(String link) {
        linkText.setTextColor(Color.parseColor(("#551A8B")));
        Uri uri = Uri.parse(link);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
    //fügt alle Bilder des ausgewähltem Albums in die Albumklasse im Model ein
    private void addBilderInAlbum() throws CorruptedExifDataException, NoImagesInAlbumException, IOException {
        ReadExif bilderReader = new ReadExif(context, downloaded, gameApplication.getGame().getAlbum().getName());
        bilderReader.startGetInhaltInOrdner();
        if (!bilderReader.anyException()) {
            for (Bild bild : bilderReader.getBilder()) {
                gameApplication.getGame().getAlbum().addBild(bild);
            }
        }
    }
    public void refreshImage(String file) {
        if (downloaded) {
            bild.setImageDrawable(Drawable.createFromPath(getFilesDir() + "/Album/" + gameApplication.getGame().getAlbum().getName() + "/" + file));
        } else {
            try (InputStream in = getAssets().open("albums/" + gameApplication.getGame().getAlbum().getName() + "/" + file)) {
                bild.setImageDrawable(Drawable.createFromStream(in, "src"));
            } catch (IOException e) {
                Log.i("Error", "Fehler beim Laden des Bildes");
                Toast.makeText(this, "Fehler beim Laden des Bildes", Toast.LENGTH_SHORT).show();
                gameApplication.getGame().stopTimer();
                finish();
            }
        }
    }
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String changeName = propertyChangeEvent.getPropertyName();
        if (changeName.equals("Beschreibung")) {
            beschreibung.setText((String) propertyChangeEvent.getNewValue());
            Log.i("Beschreibung", (String) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Bild")) {
            refreshImage(((Bild) propertyChangeEvent.getNewValue()).getName());
            Log.i("Bildname", ((Bild) propertyChangeEvent.getNewValue()).getName());
            Log.i("Laengengrad", "" + ((Bild) propertyChangeEvent.getNewValue()).getLongtitude());
            Log.i("Breitengrad", "" + ((Bild) propertyChangeEvent.getNewValue()).getLatitude());
        }
        if (changeName.equals("Punkte")) {
            aktuellerScore.setText("" + ((int) propertyChangeEvent.getNewValue()));
        }
        if (changeName.equals("Gesamtscore")) {
            gesScore.setText("" + (int) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Link")) {
            Link link = (Link) propertyChangeEvent.getNewValue();
            linkText.setText(link.getLink());
            linkText.setTextColor(Color.parseColor(link.getColor()));
            Log.i("OpenStreetMaps-Link zur Schätzung", link.getLink());
        }
        if (changeName.equals("Runde")) {
            runde.setText("" + (int) propertyChangeEvent.getNewValue());
            distanzText.setText("keine Schaetzung abgegeben");
            List<GeoPoint> weltKarte = new ArrayList<>();
            weltKarte.add(new GeoPoint(WELTBREITE, WELTLAENGE));
            weltKarte.add(new GeoPoint(-WELTBREITE, -WELTLAENGE));
            karte.post(() -> {
                karte.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(weltKarte), false);
            });
        }
        if (changeName.equals("markerDisappear")) {
            longitudeField.setText(null);
            latitudeField.setText(null);
            karte.getOverlays().remove(markerCorrect);
            karte.getOverlays().remove(nutzerMarker);
            karte.getOverlays().remove(line);
            markerCorrect.closeInfoWindow();
            nutzerMarker.closeInfoWindow();
        }
        if (changeName.equals("Timer")) {
            runOnUiThread(()->{
                timerText.setText("" + (int) propertyChangeEvent.getNewValue());
            });
        }
        if (changeName.equals("EndGame")) {
            Intent intent = new Intent(SpielansichtActivity.this, EndActivity.class);
            startActivity(intent);
        }
        if (changeName.equals("UserMarker")) {
            karte.getOverlays().remove(nutzerMarker);
            MarkerKoordinaten userMarker = (MarkerKoordinaten) propertyChangeEvent.getNewValue();
            nutzerMarker.setPosition(new GeoPoint(userMarker.getLatitude(), userMarker.getLongitude()));
            karte.getOverlays().add(nutzerMarker);
            String latitudeText = Double.toString(userMarker.getLatitude());
            latitudeField.setText(latitudeText);
            Log.i("Breitengrad des gesetzen Markers", latitudeText);
            String longitudeText = Double.toString(userMarker.getLongitude());
            longitudeField.setText(longitudeText);
            Log.i("Laengengrad des gesetzen Markers", longitudeText);
        }
        if (changeName.equals("desiredMarker")) {
            karte.getOverlays().remove(markerCorrect);
            MarkerKoordinaten desiredMarker = (MarkerKoordinaten) propertyChangeEvent.getNewValue();
            markerCorrect.setPosition(new GeoPoint(desiredMarker.getLatitude(), desiredMarker.getLongitude()));
            karte.getOverlays().add(markerCorrect);
        }
        if (changeName.equals("Alert")) {
            runOnUiThread(()->{
                Toast.makeText(getApplicationContext(), (String) propertyChangeEvent.getNewValue(), Toast.LENGTH_SHORT).show();
            });
        }
        if (changeName.equals("Distanz")) {
            Log.i("Distanz der Schaetzung", (String) propertyChangeEvent.getNewValue());
            distanzText.setText((String) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Valid")) {
            List<GeoPoint> geoPoints = new ArrayList<>();
            geoPoints.add(new GeoPoint(nutzerMarker.getPosition()));
            geoPoints.add(new GeoPoint(markerCorrect.getPosition()));
            line.setPoints(geoPoints);
            karte.getOverlays().add(line);
            karte.post(() -> {
                karte.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(geoPoints).increaseByScale(ZOOMWELT), true);
            });
        }
    }

    //Wenn die Activity beeendet wird, muss der Listener entfernt werden
    @Override
    protected void onDestroy() {
        gameApplication.getGame().removeListener(this);
        super.onDestroy();
    }
}
