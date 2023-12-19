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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Das ist die DownloadActivity, die ausgeführt wird wenn man auf Download in der MainActivity drückt
 */
public class DownloadActivity extends AppCompatActivity {
    Button anfrageButton;
    Button zurueckButton;
    Button downloadButton;
    Spinner spinner;
    Boolean keineAuswahl = true;
    TextView anzahlBilder;
    AlbenListe listeAnAlben;
    int positionSpinner = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        anfrageButton = findViewById(R.id.anfrageButton);
        spinner = findViewById(R.id.spinnerDownload);
        anzahlBilder = findViewById(R.id.anzahlBilderText);
        zurueckButton = findViewById(R.id.backButton);
        downloadButton = findViewById(R.id.downloadButton);
        //Das passiert wenn man auf den Anfragen Button drückt
        anfrageButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button){
                final TextView urlText = findViewById(R.id.urlText);
                final String url = Objects.requireNonNull(urlText.getText()).toString();
                final StringRequest request = buildRequestAlbenListe(url);
                final RequestQueue queue = Volley.newRequestQueue(DownloadActivity.this);
                queue.add(request);
            }
        });
        //Das passiert wenn man auf den Zurück Button drückt
        zurueckButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button){
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
        //Das passiert wenn man auf den Download Button drückt
        downloadButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button){
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
                keineAuswahl = position == 0;
                positionSpinner = position;
                if (keineAuswahl) {
                    anzahlBilder.setText("0");
                } else {
                    Log.i("ausgewaehltes Album",spinner.getSelectedItem().toString());
                    anzahlBilder.setText("" + listeAnAlben.getAlbums()[position-1].getNumImages());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                keineAuswahl = true;
            }
        });
    }
    //maybe Loading anzeige während es läd?
    //Hilffunktion, die die Alben Liste in listeAnAlben schreibt
    public StringRequest buildRequestAlbenListe(final String url) {
        final Response.Listener<String> onResponse = response -> {
            listeAnAlben = AlbenListe.fromJson(response);
            if (listeAnAlben == null) {
                Toast.makeText(this, "Unter dieser Adresse befindet sich keine Albenliste", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, listeAnAlben.getMessage(), Toast.LENGTH_SHORT).show();
                spinner.setAdapter(new ArrayAdapter(this,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, listeAnAlben.getNames()));
            }
        };
        final Response.ErrorListener onError = error -> {
            spinner.setAdapter(new ArrayAdapter(this,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, new String[]{ "keine Alben vorhanden" }));
            Toast.makeText(this, "Es konnte keine Verbindung zum Server aufgebaut werden!", Toast.LENGTH_SHORT).show();
        };
        return new StringRequest(Request.Method.GET, url, onResponse, onError);
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
            Toast.makeText(this, "Es konnte keine Verbindung zu den Bildern, im Album hergestellt werden", Toast.LENGTH_SHORT).show();
        };
        return new StringRequest(Request.Method.GET, url, onResponse, onError);
    }
    //Hilffunktion, die den Alben-Download-Ordner erstellt und nach ueberschreibung fragt, wenn nötig
    public void erstelleOrdner(final ServerAlbum serverAlbum) {
        final File albumFolder = new File(getFilesDir(), "Album");
        if (!albumFolder.exists()) {
            albumFolder.mkdirs();
        }
        final File folder = new File(getFilesDir() + "/Album", serverAlbum.getName());
        if (folder.exists()) {
            AlertDialog.Builder uberschreib = new AlertDialog.Builder(this);
            uberschreib.setMessage("Ein Ordner mit diesem Namen existiert bereits. Soll dieser ueberschrieben werden?")
                    .setPositiveButton("Ja", (dialog, id) -> {
                        erstelleOrdnerUndBilder(serverAlbum, folder);
                    })
                    .setNegativeButton("Abbruch", (dialog, id) -> {})
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
            String location = getFilesDir() + "/Album" + "/" + serverAlbum.getName() + "/" + serverAlbum.getImages()[i].getId() + getExtensionFromMimeType(mimeType);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveImage(url, location);
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Bild konnte nicht geladen werden", Toast.LENGTH_SHORT).show();
                    }
                }
            }).start();
        }
    }
    //Hilffunktion, die die ein einzelnes Bild erstellt
    // https://www.avajava.com/tutorials/lessons/how-do-i-save-an-image-from-a-url-to-a-file.html
    public void saveImage(String imageUrl, String destinationFile) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = Files.newOutputStream(Paths.get(destinationFile));

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != - 1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }
    //Hilffunktion, die den Typen des bilder ausgibt
    private String getExtensionFromMimeType(String mimeType) {
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
        final StringRequest request = buildRequestImages(listeAnAlben.getAlbums()[positionSpinner - 1].getLink());
        final RequestQueue queue = Volley.newRequestQueue(DownloadActivity.this);
        queue.add(request);
    }
}
