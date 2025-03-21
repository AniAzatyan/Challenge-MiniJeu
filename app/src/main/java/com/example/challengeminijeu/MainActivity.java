package com.example.challengeminijeu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(v -> showGameSettingsDialog());

    }

    private void showGameSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_game_settings, null);
        builder.setView(view);

        NumberPicker handsPicker = view.findViewById(R.id.handsPicker);
        NumberPicker fingersPicker = view.findViewById(R.id.fingersPicker);

        final int maxFingersTotal = 15;

        handsPicker.setMinValue(1);
        handsPicker.setMaxValue(5);

        fingersPicker.setMinValue(2);
        fingersPicker.setMaxValue(4);

        handsPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            int newMaxFingers = maxFingersTotal / newVal;
            newMaxFingers = Math.min(newMaxFingers, 4);
            fingersPicker.setMaxValue(Math.max(2, newMaxFingers));
        });

        fingersPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            int newMaxHands = maxFingersTotal / newVal;
            handsPicker.setMaxValue(Math.min(5, newMaxHands));
        });

        builder.setTitle("Configurer la partie");

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            Button startButton = view.findViewById(R.id.startButton);
            Button cancelButton = view.findViewById(R.id.cancelButton);

            startButton.setOnClickListener(btn -> {
                int hands = handsPicker.getValue();
                int fingers = fingersPicker.getValue();
                int total = hands * fingers;

                if (total > maxFingersTotal) {
                    Toast.makeText(this, "Maximum 15 doigts autorisés", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    startGame(fingers, hands);
                }
            });

            cancelButton.setOnClickListener(btn -> dialog.dismiss());
        });

        dialog.show();
    }

    private void startGame(int fingers, int hands) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            setContentView(new GameView(this, fingers, hands));
        }
    }

}
