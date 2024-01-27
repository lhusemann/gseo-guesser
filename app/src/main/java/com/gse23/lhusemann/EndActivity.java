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
    //Diese Methode wird beim Start ausgeführt
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        gameApplication = (GameApplication) getApplication();
        gameApplication.getGame().addListener(this);
        TextView gesamtpunkte = findViewById(R.id.gesPunkteID);
        gesamtpunkte.setText(gameApplication.getGame().getGesamtscoreString());
        erstelleRundenScores();
        Button restart = findViewById(R.id.restartButton);
        //Das soll passieren, wenn auf Neustarten gedrückt wird
        restart.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                gameApplication.getGame().restart();
                Intent intent = new Intent(EndActivity.this, SpielansichtActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        Button mainMenu = findViewById(R.id.mainmenuButton);
        //Das soll passieren, wenn auf Startansicht gedrückt wird
        mainMenu.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                gameApplication.getGame().aufgeben();
                Intent intent = new Intent(EndActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
    //Hilffunktion, die den Score in jeder Runde darstellt
    private void erstelleRundenScores() {
        ArrayList<Integer> scores = gameApplication.getGame().getPunkte().giveAllScores();
        TextView rundenTexte = findViewById(R.id.rundenText);
        TextView rundenScores = findViewById(R.id.rundenScores);
        StringBuilder scoreTexte = new StringBuilder();
        StringBuilder rundenText = new StringBuilder();
        for (int i = 0; i < scores.size(); i++) {
            scoreTexte.append(scores.get(i)).append("\n");
            rundenText.append("Runde " + (i + 1) + ": \n");
        }
        rundenTexte.setText(rundenText);
        rundenScores.setText(scoreTexte);
    }
    //Wenn in einem Model eine Sache verändert wird, wird hier gesagt, was passieren soll
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
    }
    //Wenn die Activity beeendet wird, müssen die Listener entfernt werden
    @Override
    protected void onDestroy() {
        gameApplication.getGame().removeListener(this);
        super.onDestroy();
    }
}
