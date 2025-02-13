package com.example.kd;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameActivity extends AppCompatActivity {

    private TextView infoTextView;
    private LinearLayout answersLayout;
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private String playerName;
    private String serverIp = "";
    private Handler handler;

    private int requiredAnswers;
    private int selectedAnswersCount = 0;
    private ArrayList<String> selectedAnswers = new ArrayList<>();
    private ArrayList<String> availableAnswers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        infoTextView = findViewById(R.id.infoTextView);
        answersLayout = findViewById(R.id.answersLayout);

        Intent intent = getIntent();
        playerName = intent.getStringExtra("PLAYER_NAME");

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                updateUI((String) msg.obj);
                return true;
            }
        });

        promptForServerIP();
    }

    private void promptForServerIP() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Server IP");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("e.g., 192.168.1.10");
        builder.setView(input);

        builder.setPositiveButton("Connect", (dialog, which) -> {
            serverIp = input.getText().toString().trim();
            if (!serverIp.isEmpty()) {
                connectToServer();
            } else {
                infoTextView.setText("Invalid IP. Please restart the app.");
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void connectToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(serverIp, 12345);
                    output = new PrintWriter(socket.getOutputStream(), true);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    output.println(playerName);

                    String message;
                    while ((message = input.readLine()) != null) {
                        Message uiMessage = handler.obtainMessage(1, message);
                        handler.sendMessage(uiMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateUI(String message) {
        String[] parts = message.split(";");
        String eventType = parts[0];

        switch (eventType) {
            case "WAIT":
                infoTextView.setText(parts[1] + "\n" + parts[2]);
                answersLayout.removeAllViews();
                break;

            case "PYTANIE":
                infoTextView.setText(parts[parts.length - 2]);
                requiredAnswers = Integer.parseInt(parts[parts.length - 1]);
                selectedAnswersCount = 0;
                selectedAnswers.clear();
                availableAnswers.clear();

                for (int i = 2; i < 2 + Integer.parseInt(parts[1]); i++) {
                    availableAnswers.add(parts[i]);
                }

                refreshAnswersView(parts[parts.length - 2]);

                break;

            case "ODPOWIEDZI":
                infoTextView.setText("Odpowiedzi:");
                answersLayout.removeAllViews();

                for (int i = 1; i < parts.length; i++) {
                    Button answerButton = new Button(GameActivity.this);
                    answerButton.setText(parts[i]);
                    answersLayout.addView(answerButton);
                }
                break;

            case "ODPOWIEDZICHOOSE":
                infoTextView.setText("Wybierz najlepszą odpowiedź:");
                answersLayout.removeAllViews();

                for (int i = 1; i < parts.length; i++) {
                    Button answerButton = new Button(GameActivity.this);
                    answerButton.setText(parts[i]);
                    final int index = i;
                    answerButton.setOnClickListener(v -> new SendDataTask().execute(String.valueOf(index - 1)));

                    answerButton.setTextSize(25);
                    answersLayout.addView(answerButton);
                }
                break;

            case "DISPLAYWINNER":
                infoTextView.setText("Zwycięzca: " + parts[1] + "\nOdpowiedź: " + parts[2]);
                answersLayout.removeAllViews();
                break;

            case "DISPLATRANKING":
                StringBuilder ranking = new StringBuilder("Ranking:\n");
                for (int i = 1; i < parts.length; i++) {
                    ranking.append(parts[i]).append("\n");
                }
                infoTextView.setText(ranking.toString());
                answersLayout.removeAllViews();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) {
                socket.close();
            }
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SendDataTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            if (strings.length > 0) {
                String dataToSend = strings[0];
                output.println(dataToSend);
            }
            return null;
        }
    }
    public static String replaceGaps(String zdanie, ArrayList<String> tekstyDoWstawienia) {
        String regex = "\\.{4,}";
        StringBuffer noweZdanie = new StringBuffer();
        int index = 0;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(zdanie);

        while (matcher.find() && index < tekstyDoWstawienia.size()) {
            matcher.appendReplacement(noweZdanie, tekstyDoWstawienia.get(index++));
        }
        matcher.appendTail(noweZdanie);

        return noweZdanie.toString();
    }
    private void refreshAnswersView(String q) {
        answersLayout.removeAllViews();

        for (String answer : availableAnswers) {
            Button answerButton = new Button(GameActivity.this);
            answerButton.setText(answer);
            answerButton.setTextSize(25);
            answerButton.setOnClickListener(v -> {
                selectedAnswers.add(answer);
                availableAnswers.remove(answer);
                selectedAnswersCount++;

                if (selectedAnswersCount == requiredAnswers) {
                    answersLayout.removeAllViews();
                    new SendDataTask().execute(String.join(";", selectedAnswers));
                    infoTextView.setText("Wysłano odpowiedzi.");
                } else {
                    infoTextView.setText(replaceGaps(q, selectedAnswers));
                    refreshAnswersView(q);
                }
            });
            answersLayout.addView(answerButton);
        }
    }

}
