package com.gse23.lhusemann;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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

    GameApplication gameApplication;
    Button nextButton;
    Button aufgebenButton;
    TextView longitudeField;
    TextView latitudeField;
    TextView linkText;
    MapView karte;
    Marker nutzerMarker;
    Marker markerCorrect;
    Polyline line;
    Context context;
    IConfigurationProvider provider;
    boolean downloaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);
        gameApplication = (GameApplication) getApplication();
        gameApplication.getGame().addListener(this);
        nextButton = findViewById(R.id.nextButton);
        aufgebenButton = findViewById(R.id.ffButton);
        longitudeField = findViewById(R.id.longitudeField);
        latitudeField = findViewById(R.id.latitudeField);
        linkText = findViewById(R.id.link);
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
        nutzerMarker.setPosition(new GeoPoint(0.0, 0.0));
        nutzerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        nutzerMarker.setTitle("Ihre Eingabe");
        karte.getOverlays().remove(nutzerMarker);
        markerCorrect = new Marker(karte);
        markerCorrect.setPosition(new GeoPoint(0.0, 0.0));
        markerCorrect.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerCorrect.setTitle("gesuchter Ort");
        karte.getOverlays().remove(markerCorrect);
        Log.i("Auswahl", "Das Album " + gameApplication.getGame().getAlbum().getName() + " wurde ausgewaehlt.");
        try {
            this.addBilderInAlbum();
            gameApplication.getGame().start();
        } catch (CorruptedExifDataException e) {
            final String message = "Exifdaten in einer oder mehrerer Dateien nicht enthalten!";
            Log.i("CorruptedExifDataException", message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            leaveActivity();
        } catch (NoImagesInAlbumException e) {
            final String message = "Keine Bilder im ausgewaehltem Album vorhanden.";
            Log.i("NoImagesInAlbumException", message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            leaveActivity();
        } catch (IOException e) {
            final String message = "Fehler beim Lesen eines Bildes!";
            Log.i("Error", message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            leaveActivity();
        }

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

        Button validButton = findViewById(R.id.validButton);
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
                            leaveActivity();
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
        nextButton.setText("Nächstes Bild");
    }

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
    //fügt alle Bilder des ausgewähltem Albums zum Model hinzu
    private void addBilderInAlbum() throws CorruptedExifDataException, NoImagesInAlbumException, IOException {
        ReadExif bilderReader = new ReadExif(context, downloaded, gameApplication.getGame().getAlbum().getName());
        bilderReader.startGetInhaltInOrdner();
        if (!bilderReader.anyException()) {
            for (Bild bild : bilderReader.getBilder()) {
                gameApplication.getGame().getAlbum().addBild(bild);
            }
        }
    }
    //aktualisiert das aktuelle Bild
    public void refreshImage(String file) {
        final String internAlbum = "/Album/";
        final String message = "Fehler beim Laden des Bildes";
        final String error = "Fehler";
        ImageView bild = findViewById(R.id.bild);
        if (downloaded) {
            final File image = new File(getFilesDir() + internAlbum + gameApplication.getGame()
                    .getAlbum().getName(), file);
            if (image.exists()) {
                bild.setImageDrawable(Drawable.createFromPath(image.getAbsolutePath()));
            } else {
                Log.i(error, message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                leaveActivity();
            }
            bild.setImageDrawable(Drawable.createFromPath(getFilesDir() + internAlbum + gameApplication
                    .getGame().getAlbum().getName() + "/" + file));
        } else {
            try (InputStream in = getAssets().open("albums/" + gameApplication.getGame().getAlbum()
                    .getName() + "/" + file)) {
                bild.setImageDrawable(Drawable.createFromStream(in, "src"));
            } catch (IOException e) {
                Log.i(error, message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                leaveActivity();
            }
        }
    }
    public void leaveActivity() {
        gameApplication.getGame().aufgeben();
        finish();
    }
    public void updateBeschreibung(String beschreibung) {
        TextView beschreibungText = findViewById(R.id.beschreibungID);
        beschreibungText.setText(beschreibung);
        Log.i("Beschreibung vom Bild", beschreibung);
    }
    public void updateBild(Bild bild) {
        refreshImage(bild.getName());
        Log.i("Bildname", bild.getName());
        Log.i("Laengengrad", "" + bild.getLongtitude());
        Log.i("Breitengrad", "" + bild.getLatitude());
    }
    public void updateLink(Link link) {
        runOnUiThread(() -> {
            linkText.setText(link.getLink());
            linkText.setTextColor(Color.parseColor(link.getColor()));
        });
        Log.i("OpenStreetMaps-Link zur Schätzung", link.getLink());
    }
    public void updateRunde(int neueRunde) {
        if (neueRunde == gameApplication.getGame().getRundenZaehler().getMaxRunde()) {
            nextButton.setText("Spiel beenden");
        }
        final double weltbreite = 85.0;
        final double weltlaenge = 180.0;
        TextView runde = findViewById(R.id.rundenID);
        runde.setText("" + neueRunde);
        List<GeoPoint> weltKarte = new ArrayList<>();
        weltKarte.add(new GeoPoint(weltbreite, weltlaenge));
        weltKarte.add(new GeoPoint(-weltbreite, -weltlaenge));
        karte.post(() -> {
            karte.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(weltKarte), false);
        });
    }
    public void removeMarker() {
        longitudeField.setText(null);
        latitudeField.setText(null);
        karte.getOverlays().remove(markerCorrect);
        karte.getOverlays().remove(nutzerMarker);
        karte.getOverlays().remove(line);
        markerCorrect.closeInfoWindow();
        nutzerMarker.closeInfoWindow();
    }
    public void setUserMarker(MarkerKoordinaten userMarker) {
        karte.getOverlays().remove(nutzerMarker);
        nutzerMarker.setPosition(new GeoPoint(userMarker.getLatitude(), userMarker.getLongitude()));
        karte.getOverlays().add(nutzerMarker);
        String latitudeText = Double.toString(userMarker.getLatitude());
        latitudeField.setText(latitudeText);
        Log.i("Breitengrad des gesetzen Markers", latitudeText);
        String longitudeText = Double.toString(userMarker.getLongitude());
        longitudeField.setText(longitudeText);
        Log.i("Laengengrad des gesetzen Markers", longitudeText);
    }
    public void setDesiredMarker(MarkerKoordinaten desiredMarker) {
        karte.getOverlays().remove(markerCorrect);
        markerCorrect.setPosition(new GeoPoint(desiredMarker.getLatitude(), desiredMarker.getLongitude()));
        karte.getOverlays().add(markerCorrect);
    }
    public void zoomForGuess() {
        final float zoomwelt = 1.5f;
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(new GeoPoint(nutzerMarker.getPosition()));
        geoPoints.add(new GeoPoint(markerCorrect.getPosition()));
        line.setPoints(geoPoints);
        karte.getOverlays().add(line);
        karte.post(() -> {
            karte.zoomToBoundingBox(BoundingBox.fromGeoPointsSafe(geoPoints).increaseByScale(zoomwelt), true);
        });
    }
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String changeName = propertyChangeEvent.getPropertyName();
        if (changeName.equals("Beschreibung")) {
            updateBeschreibung((String) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Bild")) {
            updateBild((Bild) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Punkte")) {
            TextView aktuellerScore = findViewById(R.id.aktuellerScore);
            aktuellerScore.setText("" + ((int) propertyChangeEvent.getNewValue()));
        }
        if (changeName.equals("Gesamtscore")) {
            TextView gesScore = findViewById(R.id.gesScore);
            gesScore.setText("" + (int) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Link")) {
            updateLink((Link) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Runde")) {
            updateRunde((int) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("markerDisappear")) {
            removeMarker();
        }
        if (changeName.equals("Timer")) {
            TextView timerText = findViewById(R.id.timerID);
            runOnUiThread(() -> {
                timerText.setText("" + (int) propertyChangeEvent.getNewValue());
            });
        }
        if (changeName.equals("EndGame")) {
            Intent intent = new Intent(SpielansichtActivity.this, EndActivity.class);
            startActivity(intent);
        }
        if (changeName.equals("UserMarker")) {
            setUserMarker((MarkerKoordinaten) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("desiredMarker")) {
            setDesiredMarker((MarkerKoordinaten) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Alert")) {
            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), (String) propertyChangeEvent.getNewValue(),
                        Toast.LENGTH_SHORT).show();
            });
        }
        if (changeName.equals("Distanz")) {
            TextView distanzText = findViewById(R.id.textSchaetzung);
            Log.i("Distanz der Schaetzung", (String) propertyChangeEvent.getNewValue());
            distanzText.setText((String) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("Valid")) {
            zoomForGuess();
        }
    }

    //Wenn die Activity beeendet wird, muss der Listener entfernt werden
    @Override
    protected void onDestroy() {
        gameApplication.getGame().removeListener(this);
        super.onDestroy();
    }
}
