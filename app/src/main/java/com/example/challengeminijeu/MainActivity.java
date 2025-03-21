package com.example.challengeminijeu;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.challengeminijeu.models.Ranking;
import com.example.challengeminijeu.repositories.RankingRepository;
import com.google.firebase.FirebaseApp;

import java.util.UUID;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
        RankingRepository rankingRepository = new RankingRepository();
        Ranking ranking = Ranking.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .userName("Léo")
                .score(5)
                .nbHand(1)
                .nbFingers(3)
                .build();
        rankingRepository.addRanking(ranking, success -> {
            if (!success) {
                Log.d("MainActivity", "Ajout Ranking");
            }
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new GameView(this));
        SharedPreferences sharedPref =
                this.getPreferences(Context.MODE_PRIVATE);
        int valeur_y = sharedPref.getInt("valeur_y", 0);
        valeur_y = (valeur_y + 100) % 400;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("valeur_y", valeur_y);
        editor.apply();

    }
}