package com.gse23.lhusemann;

import androidx.appcompat.app.AppCompatActivity;

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
}
