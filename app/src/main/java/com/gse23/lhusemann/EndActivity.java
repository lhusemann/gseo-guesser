package com.gse23.lhusemann;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class EndActivity extends AppCompatActivity implements PropertyChangeListener {
    GameApplication gameApplication;
    TextView gesamtpunkte;
    TextView rundenTexte;
    TextView rundenScores;
    Button restart;
    Button mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        gameApplication = (GameApplication) getApplication();
        gameApplication.getRundenZaehler().addListener(this);
        gameApplication.getGesamtscore().addListener(this);
        gesamtpunkte = findViewById(R.id.gesPunkteID);
        rundenTexte = findViewById(R.id.rundenText);
        rundenScores = findViewById(R.id.rundenScores);
        restart = findViewById(R.id.restartButton);
        mainMenu = findViewById(R.id.mainmenuButton);
        gesamtpunkte.setText(gameApplication.getGesamtscore().getStringGesamtscore());
        erstelleRundenScores();

        restart.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                gameApplication.setGesamtscore(new Gesamtscore(0));
                gameApplication.setRundezaehler(new Rundenzaehler(0,gameApplication.getRundenZaehler()
                        .getMaxRunde(),gameApplication.getRundenZaehler().getRundenlaenge()));
                Intent intent = new Intent(EndActivity.this, SpielansichtActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        mainMenu.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                Intent intent = new Intent(EndActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
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
        Punkte halter = gameApplication.getPunkte();
        halter.resetScores();
        gameApplication.setPunkte(halter);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
    @Override
    protected void onDestroy() {
        gameApplication.getRundenZaehler().removeListener(this);
        gameApplication.getGesamtscore().removeListener(this);
        super.onDestroy();
    }
}