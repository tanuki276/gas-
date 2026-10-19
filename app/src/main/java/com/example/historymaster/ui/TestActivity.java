package com.example.historymaster.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.historymaster.R;
import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.data.QuizGenerator;
import com.example.historymaster.models.QuizQuestion;
import com.example.historymaster.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    private List<QuizQuestion> testQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int testScore = 0;
    private boolean answered = false;
    private boolean finished = false;
    private String region;

    private TextView tvQuestion, tvProgress;
    private final Button[] optionButtons = new Button[4];
    private Button btnNext, btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        region = JsonLoader.normalizeRegion(getIntent().getStringExtra("region"));
        initUI();
        setLoadingState(true);

        JsonLoader.loadHistoryDataAsync(this, JsonLoader.getRegionFileName(region), data -> {
            if (isFinishing() || isDestroyed()) return;
            if (data != null) testQuestions = QuizGenerator.generateQuiz(data, 20);
            if (!testQuestions.isEmpty()) {
                setLoadingState(false);
                displayQuestion(0);
            } else {
                tvQuestion.setText("テスト問題を生成できませんでした");
                Toast.makeText(this, "テスト問題を生成できませんでした", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initUI() {
        tvQuestion = findViewById(R.id.tv_question);
        tvProgress = findViewById(R.id.tv_progress);
        optionButtons[0] = findViewById(R.id.btn_option1);
        optionButtons[1] = findViewById(R.id.btn_option2);
        optionButtons[2] = findViewById(R.id.btn_option3);
        optionButtons[3] = findViewById(R.id.btn_option4);
        btnNext = findViewById(R.id.btn_next);
        btnFinish = findViewById(R.id.btn_finish);

        for (int i = 0; i < optionButtons.length; i++) {
            final int index = i;
            optionButtons[i].setOnClickListener(v -> selectAnswer(index));
        }
        btnNext.setOnClickListener(v -> nextQuestion());
        btnFinish.setOnClickListener(v -> finishTest());
    }

    private void setLoadingState(boolean loading) {
        setAnswerButtonsEnabled(!loading);
        btnNext.setEnabled(false);
        btnFinish.setEnabled(!loading);
        if (loading) {
            tvQuestion.setText("問題を準備中…");
            tvProgress.setText("");
        }
    }

    private void displayQuestion(int index) {
        if (index >= testQuestions.size()) {
            finishTest();
            return;
        }
        answered = false;
        QuizQuestion question = testQuestions.get(index);
        tvQuestion.setText(question.getQuestion());
        tvProgress.setText("問題 " + (index + 1) + "/" + testQuestions.size());

        List<String> options = question.getOptions();
        for (int i = 0; i < optionButtons.length; i++) {
            Button button = optionButtons[i];
            if (i < options.size()) {
                button.setText(options.get(i));
                button.setVisibility(View.VISIBLE);
                button.setEnabled(true);
                button.setBackgroundColor(getColor(R.color.button_normal));
            } else {
                button.setVisibility(View.GONE);
            }
        }
        btnNext.setEnabled(false);
    }

    private void selectAnswer(int index) {
        if (answered || finished || index < 0 || index >= optionButtons.length) return;
        QuizQuestion question = testQuestions.get(currentQuestionIndex);
        if (index >= question.getOptions().size()) return;

        answered = true;
        boolean isCorrect = question.checkAnswer(index);
        if (isCorrect) testScore += 5;
        highlightAnswer(index, isCorrect);
        setAnswerButtonsEnabled(false);
        btnNext.setEnabled(true);

        if (question.getExplanation() != null) {
            Toast.makeText(this, question.getExplanation(), Toast.LENGTH_SHORT).show();
        }
    }

    private void highlightAnswer(int selectedIndex, boolean isCorrect) {
        QuizQuestion question = testQuestions.get(currentQuestionIndex);
        int correctIndex = question.getCorrectIndex();
        optionButtons[selectedIndex].setBackgroundColor(getColor(
            isCorrect ? R.color.correct_answer : R.color.wrong_answer));
        if (!isCorrect && correctIndex >= 0 && correctIndex < optionButtons.length) {
            optionButtons[correctIndex].setBackgroundColor(getColor(R.color.correct_answer));
        }
    }

    private void setAnswerButtonsEnabled(boolean enabled) {
        for (Button button : optionButtons) button.setEnabled(enabled);
    }

    private void nextQuestion() {
        if (!answered || finished) return;
        currentQuestionIndex++;
        if (currentQuestionIndex < testQuestions.size()) displayQuestion(currentQuestionIndex);
        else finishTest();
    }

    private void finishTest() {
        if (finished || testQuestions.isEmpty()) return;
        finished = true;
        SharedPrefsHelper.saveTestScore(this, region, testScore);
        int max = testQuestions.size() * 5;
        int best = SharedPrefsHelper.getTestScore(this, region);
        new AlertDialog.Builder(this)
            .setTitle("テスト結果")
            .setMessage(testScore + " / " + max + "点\nベストスコア: " + best + "点")
            .setPositiveButton("終了", (d, w) -> finish())
            .setCancelable(false)
            .show();
    }
}
