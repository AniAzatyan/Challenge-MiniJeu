package com.example.challengeminijeu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class EndGameActivity extends Activity {

    public static final String EXTRA_WINNER_NAME = "winner_name";
    public static final String EXTRA_DURATION_MS = "duration_ms";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        TextView winnerText = findViewById(R.id.text_winner);
        TextView durationText = findViewById(R.id.text_duration);
        TextView rankingText = findViewById(R.id.text_ranking);
        Button returnButton = findViewById(R.id.button_return);

        Intent intent = getIntent();
        String winnerName = intent.getStringExtra(EXTRA_WINNER_NAME);
        long durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 0);

        long minutes = (durationMs / 1000) / 60;
        long seconds = (durationMs / 1000) % 60;

        winnerText.setText("Gagnant : " + winnerName);
        durationText.setText("Durée de la partie : " + minutes + "m " + seconds + "s");

        // TODO: Remplacer ceci par la récupération réelle du classement avec les rounds de défaite
        rankingText.setText("Classement (simulé) :\n1. " + winnerName + " (Vainqueur)\n2. Joueur 2 - éliminé au round X\n3. Joueur 3 - éliminé au round X");

        returnButton.setOnClickListener(v -> {
            Intent backToMain = new Intent(EndGameActivity.this, MainActivity.class);
            backToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(backToMain);
            finish();
        });
    }
}