package com.example.challengeminijeu;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.challengeminijeu.models.Ranking;
import com.example.challengeminijeu.repositories.RankingRepository;
import com.google.firebase.FirebaseApp;

import java.util.UUID;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final int max_fingers = 15;
    private int selectedFingers = 0;
    private int selectedHands = 0;
    private Button lastSelectedFingerButton = null;
    private Button lastSelectedHandButton = null;
    private Button startGameButton;

    private Button finger2, finger3, finger4;
    private Button hand1, hand2, hand3, hand4, hand5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            setContentView(R.layout.activity_main);

        }
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref =
                this.getPreferences(Context.MODE_PRIVATE);
        int valeur_y = sharedPref.getInt("valeur_y", 0);
        valeur_y = (valeur_y + 100) % 400;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("valeur_y", valeur_y);
        editor.apply();

        Button viewAllScoresButton = findViewById(R.id.viewAllScoresButton);
        viewAllScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Viewing all scores...", Toast.LENGTH_SHORT).show();
            }
        });

        startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Starting the game...", Toast.LENGTH_SHORT).show();
                startGame();
            }
        });
        // Initialize finger and hand buttons
        finger2 = findViewById(R.id.finger2);
        finger3 = findViewById(R.id.finger3);
        finger4 = findViewById(R.id.finger4);

        hand1 = findViewById(R.id.hand1);
        hand2 = findViewById(R.id.hand2);
        hand3 = findViewById(R.id.hand3);
        hand4 = findViewById(R.id.hand4);
        hand5 = findViewById(R.id.hand5);

        // Initially enable all buttons
        enableAllFingerButtons();
        enableAllHandButtons();
        startGameButton.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Permission micro accordée");
            setContentView(R.layout.activity_main);
        } else {
            Log.w("MainActivity", "Permission micro refusée");
        }


        SharedPreferences sharedPref =
                this.getPreferences(Context.MODE_PRIVATE);
        int valeur_y = sharedPref.getInt("valeur_y", 0);
        valeur_y = (valeur_y + 100) % 400;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("valeur_y", valeur_y);
        editor.apply();

        Button viewAllScoresButton = findViewById(R.id.viewAllScoresButton);
        viewAllScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Viewing all scores...", Toast.LENGTH_SHORT).show();
            }
        });

        startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Starting the game...", Toast.LENGTH_SHORT).show();
                startGame();
            }
        });
        // Initialize finger and hand buttons
        finger2 = findViewById(R.id.finger2);
        finger3 = findViewById(R.id.finger3);
        finger4 = findViewById(R.id.finger4);

        hand1 = findViewById(R.id.hand1);
        hand2 = findViewById(R.id.hand2);
        hand3 = findViewById(R.id.hand3);
        hand4 = findViewById(R.id.hand4);
        hand5 = findViewById(R.id.hand5);

        // Initially enable all buttons
        enableAllFingerButtons();
        enableAllHandButtons();
        startGameButton.setEnabled(false);
    }

    public void onNumberClick(View view) {
        int number = Integer.parseInt(((Button) view).getText().toString());
        int id = view.getId();

        Button clickedButton = (Button) view;

        if ((id == R.id.finger2 || id == R.id.finger3 || id == R.id.finger4) && lastSelectedFingerButton != null) {
            lastSelectedFingerButton.setBackgroundResource(R.drawable.number_button);
        } else if ((id == R.id.hand1 || id == R.id.hand2 || id == R.id.hand3 || id == R.id.hand4 || id == R.id.hand5) && lastSelectedHandButton != null) {
            lastSelectedHandButton.setBackgroundResource(R.drawable.number_button);
        }

        // Highlight the selected button
        clickedButton.setBackgroundColor(Color.GREEN);

        if (id == R.id.finger2 || id == R.id.finger3 || id == R.id.finger4) {
            selectedFingers = number;
            lastSelectedFingerButton = clickedButton;
        } else if (id == R.id.hand1 || id == R.id.hand2 || id == R.id.hand3 || id == R.id.hand4 || id == R.id.hand5) {
            selectedHands = number;
            lastSelectedHandButton = clickedButton;
        }
        if (id == R.id.finger2 || id == R.id.finger3 || id == R.id.finger4) {
            selectedFingers = number;
            lastSelectedFingerButton = clickedButton;
            updateHandButtons();
        } else if (id == R.id.hand1 || id == R.id.hand2 || id == R.id.hand3 || id == R.id.hand4 || id == R.id.hand5) {
            selectedHands = number;
            lastSelectedHandButton = clickedButton;
            updateFingerButtons();

        }
        // Enable or disable the start game button based on the condition
        if (selectedFingers > 0 && selectedHands > 0 && selectedFingers * selectedHands < max_fingers) {
            startGameButton.setEnabled(true);
        } else {
            startGameButton.setEnabled(false);
        }

    }

    private void updateFingerButtons() {
        finger2.setEnabled(selectedHands == 0 || selectedHands * 2 <= max_fingers);
        finger3.setEnabled(selectedHands == 0 || selectedHands * 3 <= max_fingers);
        finger4.setEnabled(selectedHands == 0 || selectedHands * 4 <= max_fingers);
    }

    private void updateHandButtons() {
        hand1.setEnabled(selectedFingers == 0 || selectedFingers * 1 <= max_fingers);
        hand2.setEnabled(selectedFingers == 0 || selectedFingers * 2 <= max_fingers);
        hand3.setEnabled(selectedFingers == 0 || selectedFingers * 3 <= max_fingers);
        hand4.setEnabled(selectedFingers == 0 || selectedFingers * 4 <= max_fingers);
        hand5.setEnabled(selectedFingers == 0 || selectedFingers * 5 <= max_fingers);
    }


    private void enableAllFingerButtons() {
        finger2.setEnabled(true);
        finger3.setEnabled(true);
        finger4.setEnabled(true);
    }

    private void enableAllHandButtons() {
        hand1.setEnabled(true);
        hand2.setEnabled(true);
        hand3.setEnabled(true);
        hand4.setEnabled(true);
        hand5.setEnabled(true);
    }

    private void startGame() {
        setContentView(new GameView(this, selectedFingers, selectedHands));
    }


}
