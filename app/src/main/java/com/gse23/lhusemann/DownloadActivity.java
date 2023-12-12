package com.gse23.lhusemann;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Objects;

public class DownloadActivity extends AppCompatActivity {
    Button anfrageButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        anfrageButton = findViewById(R.id.anfrageButton);
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
    }

    public StringRequest buildRequest(final String url, final String name) {
        final String finalUrl = url + "?name=" + name;
        final Response.Listener<String> onResponse = response -> {
            AlbenListe listeAnAlben = AlbenListe.fromJson(response);
        };
        final Response.ErrorListener onError = error -> {
            Toast.makeText(this, new String("Es konnte keine Verbindung zum Server aufgebaut werden!"), Toast.LENGTH_SHORT).show();
        };
        return new StringRequest(Request.Method.GET, finalUrl, onResponse, onError);
    }

}