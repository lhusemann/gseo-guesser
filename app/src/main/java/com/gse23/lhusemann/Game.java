package com.gse23.lhusemann;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    private PropertyChangeSupport support;
    String folder;
    TimerTask timerTask;
    Timer timer;
    int counter;
    boolean rundeVorbei = false;
    boolean zeitAbgelaufen = false;
    boolean firstSkip = true;
    private static final String NOGUESS = "Kein Guess abgegeben.";
    Game() {
        this.support = new PropertyChangeSupport(this);
    }
    public void start() {
        if (firstSkip) {
            this.support.firePropertyChange("start", true, false);
            firstSkip = false;
        }
    }
    public void restart() {
        firstSkip = true;
        start();
    }
    public void mapClick() {
        if (!rundeVorbei) {
            this.support.firePropertyChange("NewMarkCheck", false, true);
        }
    }
    public void validClick(double longitude, double latitude) {
        if (rundeVorbei) {
            this.support.firePropertyChange("snackbar", "", "Runde vorbei!");
        } else {
            if (Formatierung.isValidLongitude(longitude) && Formatierung.isValidLatitude(latitude)) {
                this.support.firePropertyChange("valid", false, true);
            } else {
                if (!zeitAbgelaufen) {
                    this.support.firePropertyChange("snackbar", "", "Die eingegebenen Werte sind nicht Valide! Der" +
                            " Längengrad darf nur zwischen -180 und +180, und der Breitengrad zwischen -90 und +90 liegen!");
                }
            }
        }
    }
    public void center() {

    }
    public void aufgeben() {

    }
    public void linkClick(String link) {
        if (!link.equals(NOGUESS)) {
            this.support.firePropertyChange("linkClick", false, "#551A8B");
        }
    }
    public void zeitVorbei() {
        zeitAbgelaufen = true;
        this.support.firePropertyChange("valid", false, true);
        rundeVorbei(true);
    }
    public void rundeVorbei(boolean vorbei) {
        rundeVorbei = vorbei;
    }

    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
/*
        // folgendes passiert bei clicken auf die Map
        karte.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint clickedP) {
                if (!rundeVorbei) {
                    gameApplication.getMarkerKoordinaten().setLatitude(clickedP.getLatitude());
                    gameApplication.getMarkerKoordinaten().setLongitude(clickedP.getLongitude());
                    karte.getOverlays().remove(nutzerMarker);
                    karte.getOverlays().add(nutzerMarker);
                    karte.invalidate();
                }
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint clickedP) {
                return false;
            }
        }));
        // da ich direkt bei einem Bild starten möchte drücke ich die next Taste einmal manuell beim Start
        nextButton.performClick();
        firstSkip = false;
        //Das soll passieren wenn wir auf Bestätigen drücken
        validButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (rundeVorbei) {
                    Snackbar.make(button, "Runde vorbei!", Snackbar.LENGTH_SHORT).show();
                } else {
                    if (Formatierung.isValidLongitude(longitudeField.getText().toString())
                            && Formatierung.isValidLatitude(latitudeField.getText().toString())) {
                        stopTimer();
                        gameApplication.getMarkerKoordinaten().setLongitude(Double.parseDouble(longitudeField
                                .getText().toString()));
                        gameApplication.getMarkerKoordinaten().setLatitude(Double.parseDouble(latitudeField
                                .getText().toString()));
                        gameApplication.getDistanz().calcDistance(gameApplication.getMarkerKoordinaten()
                                .getLatitude(), gameApplication.getAlbum().getBild(gameApplication.getRundenZaehler()
                                .getRunde() - 1).getLatitude(), gameApplication.getMarkerKoordinaten()
                                .getLongitude(), gameApplication.getAlbum().getBild(gameApplication
                                .getRundenZaehler().getRunde() - 1).getLongtitude());
                        gameApplication.getPunkte().berechne(gameApplication.getDistanz().getDistance());
                        gameApplication.getGesamtscore().incGesamtscore(gameApplication.getPunkte().getPunkte());
                    } else {
                        if (!vielleichtKeinWertEinloggen) {
                            AlertDialog.Builder error = new AlertDialog.Builder(SpielansichtActivity.this);
                            error.setMessage("Die eingegebenen Werte sind nicht Valide! Der Längengrad darf nur"
                                            + " zwischen" + "-180 und +180, und der Breitengrad zwischen -90 "
                                            + "und +90 liegen!")
                                    .setPositiveButton("ok", (dialog, id) -> {
                                    })
                                    .setTitle("Error");
                            AlertDialog alert = error.create();
                            alert.show();
                        }
                    }
                }
            }
        });
        Button showButton = findViewById(R.id.showButton);
        //Das soll passieren, wenn wir auf zentrieren drücken
        showButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (!latitudeField.getText().toString().equals("") || !longitudeField.getText().toString()
                        .equals("")) {
                    double lati = Double.parseDouble(latitudeField.getText().toString());
                    double longi = Double.parseDouble(longitudeField.getText().toString());
                    IMapController mapController = karte.getController();
                    nutzerMarker.setPosition(new GeoPoint(lati, longi));
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
                            stopTimer();
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
                if (!linkText.getText().equals(NOGUESS)) {
                    linkText.setTextColor(Color.parseColor("#551A8B"));
                    openLink(linkText.getText().toString());
                }
            }
        });
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
        firstSkip = true;
        nextButton.performClick();
        firstSkip = false;
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
        String[] paths = ReadExif.getPath(getApplicationContext(), gameApplication.getAlbum()
                .getName(), gameApplication.getAlbum().isDownloaded()).toArray(new String[0]);
        for (int i = 0; i < paths.length; i++) {
            Bild aktuellesBild = new Bild(ReadExif.getNames(getApplicationContext(), gameApplication.getAlbum()
                    .getName(), gameApplication.getAlbum().isDownloaded())[i], ReadExif
                    .getExifdata(paths[i], ExifInterface.TAG_IMAGE_DESCRIPTION, SpielansichtActivity
                            .this, gameApplication.getAlbum().isDownloaded()),
                    ReadExif.getExifdata(paths[i], ExifInterface.TAG_GPS_LONGITUDE, SpielansichtActivity
                            .this, gameApplication.getAlbum().isDownloaded()),
                    ReadExif.getExifdata(paths[i], ExifInterface.TAG_GPS_LATITUDE, SpielansichtActivity
                            .this, gameApplication.getAlbum().isDownloaded()));
            gameApplication.getAlbum().addBild(aktuellesBild);
        }
    }

    //gibt alle Werte von allen Bildern des ausgewähltem Album aus
    private void bilderLoggen() throws NoImagesInAlbumException {
        try {
            if (gameApplication.getAlbum().isEmptyUndLogAll()) {
                throw new NoImagesInAlbumException(gameApplication.getAlbum().getName(), SpielansichtActivity.this);
            }
        } catch (NoImagesInAlbumException e) {
            stopTimer();
        }
    }
    //startet URL, ich habe dies Methode im Internet gefunden: https://www.youtube.com/watch?v=TU-yDCgpbZw
    private void openLink(String link) {
        Uri uri = Uri.parse(link);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
    //koopiert von https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            counter = 0;
            timerText.setText("" + counter);
        }
    }
    //koopiert von https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    private void startTimer() {
        Handler timerHandler = new Handler();
        timer = new Timer();
        counter = gameApplication.getRundenZaehler().getRundenlaenge();
        timerTask = new TimerTask() {
            public void run() {
                timerHandler.post(new Runnable() {
                    public void run() {
                        timerText.setText("" + counter);
                        counter--;
                        if (counter < 0) {
                            stopTimer();
                            Toast.makeText(SpielansichtActivity.this, "Die Zeit ist abgelaufen!", Toast.LENGTH_SHORT)
                                    .show();
                            vielleichtKeinWertEinloggen = true;
                            validButton.performClick();
                            rundeVorbei = true;
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 1, SEKUNDE);
    }
    //Wenn in einem Model eine Sache verändert wird, wird hier gesagt, was passieren soll
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
            if ((int) propertyChangeEvent.getNewValue() <= gameApplication.getRundenZaehler().getMaxRunde()
                    && gameApplication.getAlbum().getSize() > 0) {
                longitudeField.setText(null);
                latitudeField.setText(null);
                beschreibung.setText(gameApplication.getAlbum().getBild((int) propertyChangeEvent.getNewValue() - 1)
                        .getDescription());
                bild.setImageDrawable(gameApplication.getAlbum().getBild((int) propertyChangeEvent.getNewValue() - 1)
                        .getDrawable(gameApplication.getAlbum().getName(), SpielansichtActivity.this, gameApplication
                                .getAlbum().isDownloaded()));
                gameApplication.getAlbum().logExifNoDec((int) propertyChangeEvent.getNewValue() - 1);
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
        }
        if (changeName.equals("DistanzCalc")) {
            Log.i("Distanz der Schaetzung", Formatierung.getUnit((double) propertyChangeEvent.getNewValue()));
            String link = "https://www.openstreetmap.org/directions?engine=fossgis_valhalla_foot&route="
                    + gameApplication.getMarkerKoordinaten().getLatitude() + KOMMA + gameApplication
                    .getMarkerKoordinaten().getLongitude() + ";" + gameApplication.getAlbum().getBild(gameApplication
                    .getRundenZaehler().getRunde() - 1).getLatitude() + KOMMA + gameApplication.getAlbum()
                    .getBild(gameApplication.getRundenZaehler().getRunde() - 1).getLongtitude();
            Log.i("OpenStreetMaps-Link zur Schätzung", link);
            linkText.setText(link);
            linkText.setTextColor(Color.parseColor("#0000EE"));
            distanzText.setText(Formatierung.getUnit((double) propertyChangeEvent.getNewValue()));

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
            rundeVorbei = true;
        }
        if (changeName.equals("resetDistanz")) {
            distanzText.setText("keine Schaetzung abgegeben");
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
        stopTimer();
        super.onDestroy();
    }
}

 */
