package com.gse23.lhusemann;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
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
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class DownloadActivity extends AppCompatActivity {
    Button anfrageButton;
    Button zurueckButton;
    Button downloadButton;
    Spinner spinner;
    Boolean keineAuswahl = true;
    TextView anzahlBilder;
    TextView beschreibung;
    AlbenListe listeAnAlben;
    int positionSpinner = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        anfrageButton = findViewById(R.id.anfrageButton);
        spinner = findViewById(R.id.spinnerDownload);
        anzahlBilder = findViewById(R.id.anzahlBilderText);
        beschreibung = findViewById(R.id.beschreibungText);
        zurueckButton = findViewById(R.id.backButton);
        downloadButton = findViewById(R.id.downloadButton);

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
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                keineAuswahl = position == 0;
                positionSpinner = position;
                if (keineAuswahl) {
                    beschreibung.setText(null);
                    anzahlBilder.setText("0");
                } else {
                    Log.i("ausgewaehltes Album",spinner.getSelectedItem().toString());
                    beschreibung.setText(listeAnAlben.getAlbums()[position-1].getName());
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
    public StringRequest buildRequestAlbenListe(final String url) {
        final Response.Listener<String> onResponse = response -> {
            listeAnAlben = AlbenListe.fromJson(response);
            if (listeAnAlben == null) {
                Toast.makeText(this, "Unter dieser Adresse befindet sich keine Albenliste", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, new String(listeAnAlben.getMessage()), Toast.LENGTH_SHORT).show();
                spinner.setAdapter(new ArrayAdapter(this,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, listeAnAlben.getNames()));
            }
        };
        final Response.ErrorListener onError = error -> {
            spinner.setAdapter(new ArrayAdapter(this,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, new String[]{ "keine Alben vorhanden" }));
            Toast.makeText(this, new String("Es konnte keine Verbindung zum Server aufgebaut werden!"), Toast.LENGTH_SHORT).show();
        };
        return new StringRequest(Request.Method.GET, url, onResponse, onError);
    }
    public StringRequest buildRequestImages(final String url) {
        final Response.Listener<String> onResponse = response -> {
            ServerAlbum serverAlbum = ServerAlbum.fromJson(response);
            if (serverAlbum == null) {
                Toast.makeText(this, new String("Das Album konnte nicht geladen werden"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, new String(serverAlbum.getMessage()), Toast.LENGTH_SHORT).show();
                //hier muss der AlertDialog hin
                erstelleOrdner(serverAlbum);
            }
        };
        final Response.ErrorListener onError = error -> {
            Toast.makeText(this, new String("Es konnte keine Verbindung zu den Bildern, im Album hergestellt werden"), Toast.LENGTH_SHORT).show();
        };
        return new StringRequest(Request.Method.GET, url, onResponse, onError);
    }
    public void erstelleOrdner(final ServerAlbum serverAlbum) {
        final File folder = new File(getFilesDir(), serverAlbum.getName());
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
    public void erstelleOrdnerUndBilder(ServerAlbum serverAlbum, File folder) {
        folder.mkdirs();
        for (int i = 0; i<serverAlbum.getImages().length;i++) {
            final int index = i;
            final String url = serverAlbum.getImages()[i].getLink();
            final String mimeType = serverAlbum.getImages()[i].getContentType();
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    File bild = new File(getFilesDir()+"/"+serverAlbum.getName(),serverAlbum.getImages()[index].getId() + getExtensionFromMimeType(mimeType));
                    try (FileOutputStream fos = new FileOutputStream(bild)) {
                        if (mimeType.equals("image/jpeg")) {
                            response.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        } else if (mimeType.equals("image/png")) {
                            response.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        } else {
                            response.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        }
                    } catch (IOException ignored) {

                    }
                }
            }, 0, 0, null, null);
            requestQueue.add(imageRequest);
        }
    }
    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType.equals("image/jpeg")) {
            return ".jpeg";
        } else if (mimeType.equals("image/png")) {
            return ".png";
        } else {
            return ".jpg";
        }
    }
    private void download() {
        final StringRequest request = buildRequestImages(listeAnAlben.getAlbums()[positionSpinner-1].getLink());
        final RequestQueue queue = Volley.newRequestQueue(DownloadActivity.this);
        queue.add(request);
    }
}
