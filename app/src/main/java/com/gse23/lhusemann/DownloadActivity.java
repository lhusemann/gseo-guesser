package com.gse23.lhusemann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Das ist die DownloadActivity, die ausgeführt wird wenn man auf Download in der MainActivity drückt
 */
public class DownloadActivity extends AppCompatActivity implements PropertyChangeListener {
    private static final int FIXEDTHREADPOOL = 10;
    private static final String INTERNERALBUMNAME = "/Album";
    private static final String KEINEAUSWAHL = "Keine Auswahl";
    GameApplication gameApplication;
    Spinner spinner;
    Boolean keineAuswahl = true;
    List<String> auswahlmoeglichkeitenListe = new ArrayList<>();
    ArrayAdapter<String> auswahlAdapter;
    int positionSpinner = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        gameApplication = (GameApplication) getApplication();
        spinner = findViewById(R.id.spinnerDownload);
        gameApplication.setService(Executors.newFixedThreadPool(FIXEDTHREADPOOL));
        auswahlmoeglichkeitenListe.add(KEINEAUSWAHL);
        auswahlAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout
                .support_simple_spinner_dropdown_item, auswahlmoeglichkeitenListe);
        spinner.setAdapter(auswahlAdapter);
        Button anfrageButton = findViewById(R.id.anfrageButton);
        //Das passiert wenn man auf den Anfragen Button drückt
        anfrageButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                final TextView urlText = findViewById(R.id.urlText);
                if (urlText.getText().toString().equals("") || urlText.getText() == null) {
                    resetSpinner();
                    Toast.makeText(getApplicationContext(), "Keine URL eingegeben!", Toast.LENGTH_SHORT).show();
                } else {
                    final String url = Objects.requireNonNull(urlText.getText()).toString();
                    buildRequestAlbenListe(url);
                }
            }
        });
        Button zurueckButton = findViewById(R.id.backButton);
        //Das passiert wenn man auf den Zurück Button drückt
        zurueckButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                AlertDialog.Builder back = new AlertDialog.Builder(DownloadActivity.this);
                back.setMessage("Zum Hauptmenü zurückkehren?")
                        .setPositiveButton("Ja", (dialog, id) -> {
                            finish();
                        })
                        .setNegativeButton("Abbruch", (dialog, id) -> {})
                        .setTitle("Zurück");
                AlertDialog alert = back.create();
                alert.show();
            }
        });
        Button downloadButton = findViewById(R.id.downloadButton);
        //Das passiert wenn man auf den Download Button drückt
        downloadButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                if (!keineAuswahl) {
                    download();
                } else {
                    Toast.makeText(getApplicationContext(), "Kein Album ausgewaehlt!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //wenn im Spinner etwas neues ausgewählt wird, wird hier damit umgegangen
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView anzahlBilder = findViewById(R.id.anzahlBilderText);
                keineAuswahl = position == 0;
                positionSpinner = position;
                if (keineAuswahl) {
                    anzahlBilder.setText("0");
                } else {
                    Log.i("ausgewaehltes Album", spinner.getSelectedItem().toString());
                    anzahlBilder.setText("" + gameApplication.getAlbenListe().getAlbums()[position - 1]
                            .getNumImages());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                keineAuswahl = true;
            }
        });
    }
    //Hilffunktion, die die Alben Liste in listeAnAlben schreibt
    public void buildRequestAlbenListe(final String url) {
        gameApplication.getService().execute(new Runnable() {
            @Override
            public void run() {
                final Response.Listener<String> onResponse = response -> {
                    gameApplication.setAlbenListe(AlbenListe.fromJson(response));
                    if (gameApplication.getAlbenListe() == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DownloadActivity.this, "Unter dieser Adresse befindet sich keine "
                                        + "Albenliste", Toast.LENGTH_SHORT).show();
                                resetSpinner();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DownloadActivity.this, gameApplication.getAlbenListe()
                                        .getMessage(), Toast.LENGTH_SHORT).show();
                                auswahlAdapter.clear();
                                auswahlmoeglichkeitenListe.addAll(Arrays.asList(gameApplication.getAlbenListe()
                                        .getNames()));
                                auswahlAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                };
                final Response.ErrorListener onError = error -> {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resetSpinner();
                            Toast.makeText(DownloadActivity.this, "Es konnte keine Verbindung zum Server "
                                    + "aufgebaut werden!", Toast.LENGTH_SHORT).show();
                        }
                    });
                };
                final StringRequest request = new StringRequest(Request.Method.GET, url, onResponse, onError);
                final RequestQueue queue = Volley.newRequestQueue(DownloadActivity.this);
                queue.add(request);
            }
        });
    }
    //Hilffunktion, die die Anfrage zu den einzelnen Server-Alben macht
    public StringRequest buildRequestImages(final String url) {
        final Response.Listener<String> onResponse = response -> {
            ServerAlbum serverAlbum = ServerAlbum.fromJson(response);
            if (serverAlbum == null) {
                Toast.makeText(this, "Das Album konnte nicht geladen werden", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, serverAlbum.getMessage(), Toast.LENGTH_SHORT).show();
                erstelleOrdner(serverAlbum);
            }
        };
        final Response.ErrorListener onError = error -> {
            Toast.makeText(this, "Es konnte keine Verbindung zu den Bildern, im Album hergestellt werden",
                    Toast.LENGTH_SHORT).show();
        };
        return new StringRequest(Request.Method.GET, url, onResponse, onError);
    }
    //Hilffunktion, die den Alben-Download-Ordner erstellt und nach ueberschreibung fragt, wenn nötig
    public void erstelleOrdner(final ServerAlbum serverAlbum) {
        final File albumFolder = new File(getFilesDir(), "Album");
        if (!albumFolder.exists()) {
            albumFolder.mkdirs();
        }
        final File folder = new File(getFilesDir() + INTERNERALBUMNAME, serverAlbum.getName());
        if (folder.exists()) {
            AlertDialog.Builder uberschreib = new AlertDialog.Builder(this);
            uberschreib.setMessage("Ein Ordner mit diesem Namen existiert bereits. Soll dieser ueberschrieben werden?")
                    .setPositiveButton("Ok", (dialog, id) -> {
                        erstelleOrdnerUndBilder(serverAlbum, folder);
                    })
                    .setNegativeButton("Nein", (dialog, id) -> {})
                    .setTitle("Überschreiben?");
            AlertDialog alert = uberschreib.create();
            alert.show();
        } else {
            erstelleOrdnerUndBilder(serverAlbum, folder);
        }
    }
    //Hilffunktion, die die Bilder erstellt
    public void erstelleOrdnerUndBilder(ServerAlbum serverAlbum, File folder) {
        folder.mkdirs();
        for (int i = 0; i < serverAlbum.getImages().length; i++) {
            final String url = serverAlbum.getImages()[i].getLink();
            final String mimeType = serverAlbum.getImages()[i].getContentType();
            String location = getFilesDir() + INTERNERALBUMNAME + "/" + serverAlbum.getName() + "/" + serverAlbum
                    .getImages()[i].getId() + getMimeType(mimeType);
            gameApplication.getService().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveImage(url, location);
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Bild konnte nicht geladen werden",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }
    //Hilffunktion, die die ein einzelnes Bild erstellt
    // https://www.avajava.com/tutorials/lessons/how-do-i-save-an-image-from-a-url-to-a-file.html
    public void saveImage(String imageUrl, String destinationFile) throws IOException {
        final int byteGroesse = 2048;
        URL url = new URL(imageUrl);
        InputStream inputStream = url.openStream();
        OutputStream outputStream = Files.newOutputStream(Paths.get(destinationFile));

        byte[] groesse = new byte[byteGroesse];
        int length;

        while ((length = inputStream.read(groesse)) != -1) {
            outputStream.write(groesse, 0, length);
        }

        inputStream.close();
        outputStream.close();
    }
    //Hilffunktion, die den Typen des bilder ausgibt
    private String getMimeType(String mimeType) {
        if (mimeType.equals("image/jpeg")) {
            return ".jpeg";
        } else if (mimeType.equals("image/png")) {
            return ".png";
        } else {
            return ".jpg";
        }
    }
    //Hilffunktion, um den download zu starten
    private void download() {
        gameApplication.getService().execute(new Runnable() {
            @Override
            public void run() {
                final StringRequest request = buildRequestImages(gameApplication.getAlbenListe()
                        .getAlbums()[positionSpinner - 1].getLink());
                final RequestQueue queue = Volley.newRequestQueue(DownloadActivity.this);
                queue.add(request);
            }
        });
    }
    public void resetSpinner() {
        auswahlAdapter.clear();
        auswahlmoeglichkeitenListe.add(KEINEAUSWAHL);
        auswahlAdapter.notifyDataSetChanged();
    }
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
