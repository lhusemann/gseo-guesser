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

/**
 * Das ist die EndActivity, die ausgeführt wird wenn ein Spiel beendet wurde
 */
public class EndActivity extends AppCompatActivity implements PropertyChangeListener {
    GameApplication gameApplication;
    TextView gesamtpunkte;
    TextView rundenTexte;
    TextView rundenScores;
    Button restart;
    Button mainMenu;
    //Diese Methode wird beim Start ausgeführt
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
        //Das soll passieren, wenn auf Neustarten gedrückt wird
        restart.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                gameApplication.getGesamtscore().setGesamtscore(0);
                gameApplication.getRundenZaehler().setRunde(0);
                Intent intent = new Intent(EndActivity.this, SpielansichtActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        //Das soll passieren, wenn auf Startansicht gedrückt wird
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
    //Hilffunktion, die den Score in jeder Runde darstellt
    private void erstelleRundenScores() {
        ArrayList<Integer> scores = gameApplication.getPunkte().giveScores();
        StringBuilder scoreTexte = new StringBuilder();
        StringBuilder rundenText = new StringBuilder();
        for (int i = 0; i < scores.size(); i++) {
            scoreTexte.append(scores.get(i)).append("\n");
            rundenText.append("Runde " + (i + 1) + ": \n");
        }
        rundenTexte.setText(rundenText);
        rundenScores.setText(scoreTexte);
        Punkte halter = gameApplication.getPunkte();
        halter.resetScores();
        gameApplication.setPunkte(halter);
    }
    //Wenn in einem Model eine Sache verändert wird, wird hier gesagt, was passieren soll
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    }
    //Wenn die Activity beeendet wird, müssen die Listener entfernt werden
    @Override
    protected void onDestroy() {
        gameApplication.getRundenZaehler().removeListener(this);
        gameApplication.getGesamtscore().removeListener(this);
        super.onDestroy();
    }
}
