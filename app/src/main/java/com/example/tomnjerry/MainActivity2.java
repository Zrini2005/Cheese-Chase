package com.example.tomnjerry;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {
    private TextView textView;
    private TextView keyPointsTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Button button=findViewById(R.id.startButton);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            startActivity(intent);
        });
        Button button1 = findViewById(R.id.rulesButton);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity2.this);
                dialog.setContentView(R.layout.ruledialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.setCancelable(false);
                if (R.style.animation!= 0) {
                    dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
                }
                Button dialogButton = dialog.findViewById(R.id.button1);
                if (dialogButton!= null) {
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }
                dialog.show();
            }
        });
        textView=findViewById(R.id.highScoreTextView);
        loadHighScore();
        keyPointsTextView = findViewById(R.id.keyPointsTextView);

        SharedPreferences sharedPreferences = getSharedPreferences("game_prefs", Context.MODE_PRIVATE);
        int keyPoints = sharedPreferences.getInt("key_points", 0);
        keyPointsTextView.setText("Keys: " + keyPoints);


    }
    private void loadHighScore() {
        SharedPreferences sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int highScore = sharedPreferences.getInt("HighScore", 0);
        textView.setText("High Score: " + highScore);
    }
}