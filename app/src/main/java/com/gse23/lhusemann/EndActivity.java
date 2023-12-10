package com.gse23.lhusemann;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class EndActivity extends AppCompatActivity {
    GameApplication gameApplication;
    TextView gesamtpunkte;
    TextView rundenTexte;
    TextView rundenScores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        gameApplication = (GameApplication) getApplication();
        gesamtpunkte = findViewById(R.id.gesPunkteID);
        rundenTexte = findViewById(R.id.rundenText);
        rundenScores = findViewById(R.id.rundenScores);
        gesamtpunkte.setText(gameApplication.getGesamtscore().getStringGesamtscore());
        erstelleRundenScores();
    }
    private void erstelleRundenScores() {
        ArrayList<Integer> scores = gameApplication.getPunkte().giveScores();
        StringBuilder scoreTexte = new StringBuilder();
        StringBuilder rundenText = new StringBuilder();
        for (int i = 0; i < scores.size(); i++) {
            scoreTexte.append(scores.get(i)).append("\n");
            rundenText.append("Runde " + (i+1) + ": \n");
        }
        rundenTexte.setText(rundenText);
        rundenScores.setText(scoreTexte);
    }
}