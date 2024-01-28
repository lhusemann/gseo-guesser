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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Das ist die DownloadActivity, die ausgeführt wird wenn man auf Download in der MainActivity drückt
 */

public class DownloadActivity extends AppCompatActivity implements PropertyChangeListener {
    private static final int FIXEDTHREADPOOL = 10;
    private static final String INTERNERALBUMNAME = "/Album";
    private static final String KEINEAUSWAHL = "Keine Auswahl";
    private static final String ALBENLISTENTAG = "albenListenReq";
    private static final String IMAGESTAG = "ImagesReq";
    GameApplication gameApplication;
    AlbenListe albenListe;
    Spinner spinner;
    boolean keineAuswahl = true;
    List<String> auswahlmoeglichkeitenListe = new ArrayList<>();
    ArrayAdapter<String> auswahlAdapter;
    int positionSpinner = 0;
    RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        gameApplication = (GameApplication) getApplication();
        gameApplication.getGame().addListener(this);
        queue = Volley.newRequestQueue(this);
        spinner = findViewById(R.id.spinnerDownload);
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
                    Toast.makeText(getApplicationContext(), "Keine Eingabe vorhanden!", Toast.LENGTH_SHORT).show();
                } else {
                    resetSpinner();
                    anfrageAlbenListe(urlText.getText().toString());
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
                            queue.cancelAll(ALBENLISTENTAG);
                            queue.cancelAll(IMAGESTAG);
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
                if (keineAuswahl) {
                    Toast.makeText(getApplicationContext(), "Kein Album ausgewaehlt!", Toast.LENGTH_SHORT).show();
                } else {
                    download(albenListe.getAlbums()[positionSpinner - 1].getLink());
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
                    anzahlBilder.setText("" + albenListe.getAlbums()[position - 1].getNumImages());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                keineAuswahl = true;
            }
        });
    }
    private void anfrageAlbenListe(String url) {
        StringRequest request = getRequestForAlbenListe(url);
        request.setTag(ALBENLISTENTAG);
        queue.add(request);
    }
    public StringRequest getRequestForAlbenListe(final String url) {
        final Response.Listener<String> onResponse = response -> {
            albenListe = DownloadAlben.jsonToAlbenListe(response);
            if (albenListe == null) {
                Toast.makeText(DownloadActivity.this, "Unter dieser Adresse befindet sich keine "
                        + "Albenliste", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DownloadActivity.this, albenListe.getMessage(),
                        Toast.LENGTH_SHORT).show();
                auswahlAdapter.clear();
                auswahlmoeglichkeitenListe.addAll(Arrays.asList(albenListe.getNames()));
                auswahlAdapter.notifyDataSetChanged();
                gameApplication.getGame().setAlbenListe(albenListe);
            }
        };
        final Response.ErrorListener onError = error -> {
            Toast.makeText(DownloadActivity.this, "Es konnte keine Verbindung zum Server "
                    + "aufgebaut werden!", Toast.LENGTH_SHORT).show();
        };
        return new StringRequest(Request.Method.GET, url, onResponse, onError);
    }
    //Hilffunktion, um den download zu starten
    private void download(String url) {
        StringRequest request = getRequestForImages(url);
        request.setTag(IMAGESTAG);
        queue.add(request);
    }
    public StringRequest getRequestForImages(final String url) {
        final Response.Listener<String> onResponse = response -> {
            ServerAlbum serverAlbum = DownloadAlben.jsonToServerAlbum(response);
            if (serverAlbum == null) {
                Toast.makeText(this, "Das Album konnte nicht geladen werden", Toast.LENGTH_SHORT).show();
            } else {
                erstelleOrdner(serverAlbum);
                Toast.makeText(this, serverAlbum.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        final Response.ErrorListener onError = error -> {
            Toast.makeText(this, "Das ausgewählte Album konnte nicht geladen werden!", Toast.LENGTH_SHORT).show();
        };
        return new StringRequest(Request.Method.GET, url, onResponse, onError);
    }



    //Hilffunktion, die den Alben-Download-Ordner erstellt und nach ueberschreibung fragt, wenn nötig
    public void erstelleOrdner(ServerAlbum serverAlbum) {
        final File albumFolder = new File(getFilesDir(), "Album");
        if (!albumFolder.exists()) {
            albumFolder.mkdirs();
        }
        final File folder = new File(getFilesDir() + INTERNERALBUMNAME, serverAlbum.getName());
        if (folder.exists()) {
            AlertDialog.Builder ueberschreib = new AlertDialog.Builder(this);
            ueberschreib.setMessage("Ein Ordner mit diesem Namen existiert bereits. Soll dieser ueberschrieben werden?")
                    .setPositiveButton("Ok", (dialog, id) -> {
                        erstelleOrdnerUndBilder(serverAlbum, folder);
                    })
                    .setNegativeButton("Nein", (dialog, id) -> {})
                    .setTitle("Überschreiben?");
            AlertDialog alert = ueberschreib.create();
            alert.show();
        } else {
            folder.mkdirs();
            erstelleOrdnerUndBilder(serverAlbum, folder);
        }
    }

    //Hilffunktion, die die Bilder erstellt
    public void erstelleOrdnerUndBilder(ServerAlbum serverAlbum, File folder) {
        ExecutorService executerService = Executors.newFixedThreadPool(FIXEDTHREADPOOL);
        for (int i = 0; i < serverAlbum.getImages().length; i++) {

            final String url = serverAlbum.getImages()[i].getLink();
            final String mimeType = serverAlbum.getImages()[i].getContentType();
            String location = getFilesDir() + INTERNERALBUMNAME + "/" + serverAlbum.getName() + "/" + serverAlbum
                    .getImages()[i].getId() + getMimeType(mimeType);
            executerService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveImage(url, location);
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Ein Bild konnte nicht geladen werden",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        executerService.shutdown();
    }
    //Hilffunktion, die die ein einzelnes Bild erstellt
    // https://www.avajava.com/tutorials/lessons/how-do-i-save-an-image-from-a-url-to-a-file.html
    public void saveImage(String imageUrl, String destinationFile) throws IOException {

        final int byteGroesse = 2048;
        final URL url = new URL(imageUrl);
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
        gameApplication.getGame().removeListener(this);
        super.onDestroy();
    }
}
