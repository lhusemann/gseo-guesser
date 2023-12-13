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
import java.util.Objects;

public class DownloadActivity extends AppCompatActivity {
    Button anfrageButton;
    Button zurueckButton;
    Spinner spinner;
    Boolean keineAuswahl = true;
    TextView anzahlBilder;
    TextView beschreibung;
    AlbenListe listeAnAlben;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        anfrageButton = findViewById(R.id.anfrageButton);
        spinner = findViewById(R.id.spinnerDownload);
        anzahlBilder = findViewById(R.id.anzahlBilderText);
        beschreibung = findViewById(R.id.beschreibungText);
        zurueckButton = findViewById(R.id.backButton);

        anfrageButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button){
                final TextView urlText = findViewById(R.id.urlText);
                final String url = Objects.requireNonNull(urlText.getText()).toString();
                final StringRequest request = buildRequest(url, "");
                final RequestQueue queue = Volley.newRequestQueue(DownloadActivity.this);
                queue.add(request);
            }
        });
        zurueckButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button){
                AlertDialog.Builder back = new AlertDialog.Builder(DownloadActivity.this);
                back.setMessage("Zum Hauptmenü zurückkehren??")
                        .setPositiveButton("Ja", (dialog, id) -> {
                            finish();
                        })
                        .setNegativeButton("Abbruch", (dialog, id) -> {})
                        .setTitle("Zurück");
                AlertDialog alert = back.create();
                alert.show();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                keineAuswahl = position == 0;
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


    public StringRequest buildRequest(final String url, final String name) {
        final String finalUrl = url + "?name=" + name;
        final Response.Listener<String> onResponse = response -> {
            listeAnAlben = AlbenListe.fromJson(response);
            //hier möchte ich sicher gehen dass respone wirklich eine AlbenListe ist null check
            spinner.setAdapter(new ArrayAdapter(this,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, listeAnAlben.getNames()));
        };
        final Response.ErrorListener onError = error -> {
            spinner.setAdapter(new ArrayAdapter(this,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, new String[]{ "keine Alben vorhanden" }));
            Toast.makeText(this, new String("Es konnte keine Verbindung zum Server aufgebaut werden!"), Toast.LENGTH_SHORT).show();
        };
        return new StringRequest(Request.Method.GET, finalUrl, onResponse, onError);
    }

}