package com.example.historymaster.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.historymaster.R;
import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.data.QuizGenerator;
import com.example.historymaster.models.HistoryData;
import com.example.historymaster.models.QuizQuestion;
import com.example.historymaster.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private List<QuizQuestion> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer timer;
    private boolean answered = false;
    private String region;

    private TextView tvQuestion, tvQuestionNumber, tvTimer, tvScore;
    private RadioGroup radioGroup;
    private Button btnSubmit, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        region = JsonLoader.normalizeRegion(getIntent().getStringExtra("region"));
        HistoryData data = JsonLoader.loadHistoryData(this, JsonLoader.getRegionFileName(region));
        if (data != null) questions = QuizGenerator.generateQuiz(data, 10);

        initUI();
        if (!questions.isEmpty()) {
            displayQuestion(0);
            startTimer();
        } else {
            Toast.makeText(this, "クイズを生成できませんでした", Toast.LENGTH_LONG).show();
            btnSubmit.setEnabled(false);
        }
    }

    private void initUI() {
        tvQuestion = findViewById(R.id.tv_question);
        tvQuestionNumber = findViewById(R.id.tv_question_number);
        tvTimer = findViewById(R.id.tv_timer);
        tvScore = findViewById(R.id.tv_score);
        radioGroup = findViewById(R.id.radio_group);
        btnSubmit = findViewById(R.id.btn_submit);
        btnNext = findViewById(R.id.btn_next);
        btnSubmit.setOnClickListener(v -> checkAnswer(false));
        btnNext.setOnClickListener(v -> nextQuestion());
        tvScore.setText("0");
    }

    private void displayQuestion(int index) {
        QuizQuestion question = questions.get(index);
        answered = false;
        tvQuestion.setText(question.getQuestion());
        tvQuestionNumber.setText("問題 " + (index + 1) + "/" + questions.size());
        radioGroup.removeAllViews();
        for (int i = 0; i < question.getOptions().size(); i++) {
            RadioButton button = new RadioButton(this);
            button.setId(i + 1);
            button.setTag(i);
            button.setText(question.getOptions().get(i));
            radioGroup.addView(button);
        }
        radioGroup.clearCheck();
        btnSubmit.setEnabled(true);
        btnNext.setEnabled(false);
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(30000, 1000) {
            @Override public void onTick(long ms) { tvTimer.setText((ms / 1000) + "秒"); }
            @Override public void onFinish() { tvTimer.setText("0秒"); checkAnswer(true); }
        }.start();
    }

    private void checkAnswer(boolean timeout) {
        if (answered || questions.isEmpty()) return;
        answered = true;
        if (timer != null) timer.cancel();

        int selectedViewId = radioGroup.getCheckedRadioButtonId();
        int selectedIndex = -1;
        if (selectedViewId != -1) {
            RadioButton selected = findViewById(selectedViewId);
            Object tag = selected == null ? null : selected.getTag();
            if (tag instanceof Integer) selectedIndex = (Integer) tag;
        }

        QuizQuestion q = questions.get(currentQuestionIndex);
        boolean correct = selectedIndex >= 0 && q.checkAnswer(selectedIndex);
        if (correct) {
            score += 10;
            tvScore.setText(String.valueOf(score));
        }

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(false);
        }
        if (q.getCorrectIndex() >= 0 && q.getCorrectIndex() < radioGroup.getChildCount()) {
            ((RadioButton) radioGroup.getChildAt(q.getCorrectIndex())).setTextColor(getColor(R.color.correct_answer));
        }

        String message = timeout ? "時間切れです" : (correct ? "正解です" : "不正解です");
        if (q.getExplanation() != null) message += "\n" + q.getExplanation();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        btnSubmit.setEnabled(false);
        btnNext.setEnabled(true);
    }

    private void nextQuestion() {
        if (!answered) return;
        currentQuestionIndex++;
        if (currentQuestionIndex >= questions.size()) {
            finishQuiz();
            return;
        }
        displayQuestion(currentQuestionIndex);
        startTimer();
    }

    private void finishQuiz() {
        if (timer != null) timer.cancel();
        SharedPrefsHelper.saveQuizScore(this, region, score);
        int max = questions.size() * 10;
        int best = SharedPrefsHelper.getQuizScore(this, region);
        new AlertDialog.Builder(this)
            .setTitle("クイズ結果")
            .setMessage(score + " / " + max + "点\nベストスコア: " + best + "点")
            .setPositiveButton("終了", (d, w) -> finish())
            .setCancelable(false)
            .show();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
