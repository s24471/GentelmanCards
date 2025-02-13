package com.example.kd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText playerNameEditText;
    private Button findGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerNameEditText = findViewById(R.id.playerNameEditText);
        findGameButton = findViewById(R.id.findGameButton);

        findGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playerName = playerNameEditText.getText().toString().trim();
                if (!playerName.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    intent.putExtra("PLAYER_NAME", playerName);
                    startActivity(intent);
                }
            }
        });
    }
}
