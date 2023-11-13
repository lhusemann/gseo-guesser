package com.gse23.lhusemann;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import android.os.Bundle;
/**
 * Das ist die SpielansichtActivity, die zuerst ausgeführt wird, wenn diese Activity gestartet wird.
 */
public class SpielansichtActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spielansicht);

    }
    ArrayList<Object> nameBreiteLaenge() {
        ArrayList<Object> bilderDaten = new ArrayList<>(); // ist das so gemeint mit Datenstruktur?
        String[] namen = getIntent().getStringArrayExtra("namen");
        bilderDaten.add(namen);
        return bilderDaten;
    }
}

