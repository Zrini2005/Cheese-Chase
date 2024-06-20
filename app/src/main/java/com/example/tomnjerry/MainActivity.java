package com.example.tomnjerry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;
    private TextView scoreTextView;
    private ImageButton leftPowerUpButton;
    private ImageButton rightPowerUpButton;
    private ImageView button,home;
    private int keyPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameView = findViewById(R.id.gameView);
        scoreTextView = findViewById(R.id.scoreTextView);
        gameView.setScoreTextView(scoreTextView);
        button=findViewById(R.id.xbutton);
        button.setOnClickListener(v -> {
            if (!gameView.isPaused()) {
                pauseGame();
                button.setImageResource(R.drawable.playbutton);

            } else {
                resumeGame();
                button.setImageResource(R.drawable.pausebutton);
            }

        });
        SharedPreferences sharedPreferences2 = getSharedPreferences("game_prefs", Context.MODE_PRIVATE);
        keyPoints = sharedPreferences2.getInt("key_points", 0);
        leftPowerUpButton = findViewById(R.id.leftPowerUpButton);
        rightPowerUpButton = findViewById(R.id.rightPowerUpButton);
        home=findViewById(R.id.home);
        home.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);

        });


        leftPowerUpButton.setOnClickListener(v -> {
            if (keyPoints >= 2) {
                gameView.makeJerryImmune();
                keyPoints -= 2;
                updateKeyPoints();
                hidePowerUpButtons();
            }
        });

        rightPowerUpButton.setOnClickListener(v -> {
            if (keyPoints >= 2) {
                gameView.giveJerryCheeseAndGun();
                keyPoints -= 2;
                updateKeyPoints();
                hidePowerUpButtons();
            }
        });

        if(keyPoints>=2) {
            showPowerUpButtons();
        }
        else{
            hidePowerUpButtons();
        }
    }
    public void showPowerUpButtons() {

        leftPowerUpButton.setVisibility(View.VISIBLE);
        rightPowerUpButton.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            leftPowerUpButton.setVisibility(View.GONE);
            rightPowerUpButton.setVisibility(View.GONE);
        }, 10000);
    }

    public void hidePowerUpButtons() {
        leftPowerUpButton.setVisibility(View.GONE);
        rightPowerUpButton.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
    private void pauseGame() {
        gameView.pause();

    }

    private void resumeGame() {
        gameView.resume();

    }
    private void updateKeyPoints() {
        SharedPreferences sharedPreferences2 = getSharedPreferences("game_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        editor2.putInt("key_points", keyPoints);
        editor2.apply();
    }
}

