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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        gameApplication = (GameApplication) getApplication();
        gameApplication.getGame().addListener(this);
        gameApplication.getGame().startResult();

        Button restart = findViewById(R.id.restartButton);
        //Das soll passieren, wenn auf Neustarten gedrückt wird
        restart.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                gameApplication.getGame().restart();
                finish();
                Intent intent = new Intent(EndActivity.this, SpielansichtActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        Button mainMenu = findViewById(R.id.mainmenuButton);
        //Das soll passieren, wenn auf Startansicht gedrückt wird
        mainMenu.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View button) {
                gameApplication.getGame().aufgeben();
                finish();
                Intent intent = new Intent(EndActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String changeName = propertyChangeEvent.getPropertyName();
        if (changeName.equals("GesamtscoreOverlay")) {
            TextView gesamtpunkte = findViewById(R.id.gesPunkteID);
            gesamtpunkte.setText((String) propertyChangeEvent.getNewValue());
        }
        if (changeName.equals("ScoreOverlay")) {
            ArrayList<?> scores = (ArrayList<?>) propertyChangeEvent.getNewValue();
            TextView rundenTexte = findViewById(R.id.rundenText);
            TextView rundenScores = findViewById(R.id.rundenScores);
            StringBuilder scoreTexte = new StringBuilder();
            StringBuilder rundenText = new StringBuilder();
            for (int i = 0; i < scores.size(); i++) {
                scoreTexte.append(scores.get(i)).append("\n");
                rundenText.append("Runde ").append(i + 1).append(": \n");
            }
            rundenTexte.setText(rundenText);
            rundenScores.setText(scoreTexte);
        }
    }

    @Override
    protected void onDestroy() {
        gameApplication.getGame().removeListener(this);
        super.onDestroy();
    }
}
