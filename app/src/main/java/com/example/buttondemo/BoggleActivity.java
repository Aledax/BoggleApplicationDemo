package com.example.buttondemo;

import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoggleActivity extends AppCompatActivity {

    final static String TAG = "BoggleActivity";
    final static int buttonInset = 30;
    final static List<Integer> wordScoreValues = Arrays.asList(0, 0, 0, 3, 5, 9, 15, 23, 33, 45);
    final static int timeLimit = 90;

    private boolean started = false;
    private int score = 0;

    private List<String> allWords;
    private List<String> playedWords;

    private Button startButton;
    private List<List<Button>> letterButtons;
    private int hoveringLetter = -1;
    private List<Integer> currentLetters;
    private List<List<Integer>> letterSelectionPaths = Arrays.asList(
            Arrays.asList(1, 3, 4),
            Arrays.asList(0, 2, 3, 4, 5),
            Arrays.asList(1, 4, 5),
            Arrays.asList(0, 1, 4, 6, 7),
            Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8),
            Arrays.asList(1, 2, 4, 7, 8),
            Arrays.asList(3, 4, 7),
            Arrays.asList(3, 4, 5, 6, 8),
            Arrays.asList(4, 5, 7)
    );
    private String currentWord;

    private TextView scoreLabel;
    private TextView timerLabel;
    private TextView feedbackLabel;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boggle);

        // Source: https://www.tutorialspoint.com/how-to-convert-inputstream-object-to-a-string-in-java
        allWords = new ArrayList<>();
        playedWords = new ArrayList<>();
        try {
            InputStream is = getResources().openRawResource(R.raw.words);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String str;
            while ((str = br.readLine()) != null) {
                allWords.add(str);
            }
        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        RequestQueue volleyQueue = Volley.newRequestQueue(BoggleActivity.this);
        String url = getResources().getString(R.string.server_address);

        letterButtons = new ArrayList<>();
        for (int r = 0; r < 3; r++) { letterButtons.add(new ArrayList<>()); }
        letterButtons.get(0).add(findViewById(R.id.boggle_button0));
        letterButtons.get(0).add(findViewById(R.id.boggle_button1));
        letterButtons.get(0).add(findViewById(R.id.boggle_button2));
        letterButtons.get(1).add(findViewById(R.id.boggle_button3));
        letterButtons.get(1).add(findViewById(R.id.boggle_button4));
        letterButtons.get(1).add(findViewById(R.id.boggle_button5));
        letterButtons.get(2).add(findViewById(R.id.boggle_button6));
        letterButtons.get(2).add(findViewById(R.id.boggle_button7));
        letterButtons.get(2).add(findViewById(R.id.boggle_button8));
        for (int i = 0; i < 9; i++) { letterButtons.get(i / 3).get(i % 3).setClickable(false); }

        scoreLabel = findViewById(R.id.boggle_score_label);
        timerLabel = findViewById(R.id.boggle_timer_label);
        feedbackLabel = findViewById(R.id.boggle_feedback_label);
        currentLetters = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            int finalI = i;
            letterButtons.get(i / 3).get(i % 3).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Intentionally empty
                }
            });
            letterButtons.get(i / 3).get(i % 3).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (started && motionEvent.getAction() == MotionEvent.ACTION_DOWN) { // First to be pressed
                        hoveringLetter = finalI;
                        ((Button) view).setBackgroundColor(getResources().getColor(R.color.white_half));
                        ((Button) view).setTextColor(getResources().getColor(R.color.blue));
                        currentLetters.add(finalI);
                        currentWord = String.valueOf(letterButtons.get(finalI / 3).get(finalI % 3).getText());
                        feedbackLabel.setText(currentWord);
                    } else if (started && motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        int x = (int) motionEvent.getRawX();
                        int y = (int) motionEvent.getRawY();
                        for (int otherI = 0; otherI < 9; otherI++) {
                            if (!letterSelectionPaths.get(hoveringLetter).contains(otherI) || currentLetters.contains(otherI)) continue;
                            Button otherButton = letterButtons.get(otherI / 3).get(otherI % 3);
                            if (isButtonInBounds(otherButton, x, y)) {
                                if (otherI != hoveringLetter) {
                                    hoveringLetter = otherI;
                                    letterButtons.get(otherI / 3).get(otherI % 3).setBackgroundColor(getResources().getColor(R.color.white_half));
                                    letterButtons.get(otherI / 3).get(otherI % 3).setTextColor(getResources().getColor(R.color.blue));
                                    currentLetters.add(otherI);
                                    currentWord += letterButtons.get(otherI / 3).get(otherI % 3).getText();
                                    feedbackLabel.setText(currentWord);
                                }
                            }
                        }
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        hoveringLetter = -1;
                        for (int i = 0; i < 9; i++) {
                            letterButtons.get(i / 3).get(i % 3).setBackgroundColor(getResources().getColor(R.color.white_faint));
                            letterButtons.get(i / 3).get(i % 3).setTextColor(getResources().getColor(R.color.white));
                        }
                        if (started) {
                            if (currentWord.length() >= 3) {
                                if (!playedWords.contains(currentWord) && allWords.contains(currentWord.toLowerCase())) {
                                    score += wordScoreValues.get(currentWord.length());
                                    scoreLabel.setText(String.valueOf(score));
                                    feedbackLabel.setText(currentWord + ": +" + wordScoreValues.get(currentWord.length()));
                                    playedWords.add(currentWord);
                                } else if (playedWords.contains(currentWord)) {
                                    feedbackLabel.setText("Already played");
                                } else {
                                    feedbackLabel.setText("Invalid word");
                                }
                            } else {
                                feedbackLabel.setText("");
                            }
                        }
                        currentLetters = new ArrayList<>();
                        currentWord = "";
                    }
                    return false;
                }
            });
        }

        startButton = findViewById(R.id.boggle_start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JsonObjectRequest boardRequest = new JsonObjectRequest(Request.Method.GET, url + "/boggle/board", null,
                        response -> {
                    try {
                        String boardString = response.getString("board");
                        String[] rowStrings = boardString.split("\n");
                        String[][] letterStrings = {{}, {}, {}};
                        for (int r = 0; r < 3; r++) { letterStrings[r] = rowStrings[r].split(","); }
                        for (int i = 0; i < 9; i++) { letterButtons.get(i / 3).get(i % 3).setText(letterStrings[i / 3][i % 3]); }
                    } catch (JSONException e) {
                        Log.w(TAG, "JSON error");
                    }
                        }, error -> Log.e(TAG, "Error: " + error.getLocalizedMessage())
                );
                volleyQueue.add(boardRequest);
                playedWords = new ArrayList<>();
                score = 0;
                startButton.setVisibility(View.INVISIBLE);
                scoreLabel.setText(String.valueOf(score));
                timerLabel.setText(String.valueOf(timeLimit));
                feedbackLabel.setText("");
                started = true;
                new CountDownTimer(timeLimit * 1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timerLabel.setText(String.valueOf(millisUntilFinished / 1000 + 1));
                    }
                    public void onFinish() {
                        timerLabel.setText("0");
                        startButton.setVisibility(VISIBLE);
                        startButton.setText("Play again");
                        for (int i = 0; i < 0; i++) { letterButtons.get(i / 3).get(i % 3).setText(""); }
                        started = false;

                        Map<String, Integer> obj = new HashMap<>();
                        obj.put("score", score);
                        JsonObjectRequest scoreRequest = new JsonObjectRequest(Request.Method.POST, url + "/boggle/score", new JSONObject(obj),
                            response -> {
                                try {
                                    String type = response.getString("type");
                                    if (type.equals("percentile")) {
                                        feedbackLabel.setText("You're in the top " + String.valueOf(response.getString("percentile")) + "%!");
                                    } else if (type.equals("win")) {
                                        feedbackLabel.setText("You got the top score!");
                                    }
                                } catch (JSONException e) {
                                    Log.w(TAG, "JSON Error");
                                }
                            }, error -> Log.e(TAG, "Error: " + error.getLocalizedMessage())
                        );
                        volleyQueue.add(scoreRequest);
                    }
                }.start();
            }
        });
    }

    // Source: https://stackoverflow.com/questions/12980156/detect-touch-event-on-a-view-when-dragged-over-from-other-view
    public boolean isButtonInBounds(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        Rect smallRect = new Rect(outRect.left + buttonInset, outRect.top + buttonInset, outRect.right - buttonInset, outRect.bottom - buttonInset);
        return smallRect.contains(x, y);
    }
}