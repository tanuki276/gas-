package com.historymaster.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.historymaster.R;
import com.historymaster.data.JsonLoader;
import com.historymaster.data.QuizGenerator;
import com.historymaster.models.HistoryData;
import com.historymaster.models.QuizQuestion;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    
    private List<QuizQuestion> testQuestions;
    private int currentQuestionIndex = 0;
    private int testScore = 0;
    private TextView tvQuestion, tvProgress;
    private Button[] optionButtons = new Button[4];
    private Button btnNext, btnFinish;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
        String region = getIntent().getStringExtra("region");
        
        HistoryData data = JsonLoader.loadHistoryData(this,
            JsonLoader.getRegionFileName(region));
        
        if (data != null) {
            // テスト用により多くの問題を生成
            testQuestions = QuizGenerator.generateQuiz(data, 20);
        }
        
        initUI();
        
        if (testQuestions != null && !testQuestions.isEmpty()) {
            displayQuestion(currentQuestionIndex);
        }
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
        
        // ボタンリスナー設定
        for (int i = 0; i < optionButtons.length; i++) {
            final int index = i;
            optionButtons[i].setOnClickListener(v -> selectAnswer(index));
        }
        
        btnNext.setOnClickListener(v -> nextQuestion());
        btnFinish.setOnClickListener(v -> finishTest());
    }
    
    private void displayQuestion(int index) {
        if (index >= testQuestions.size()) {
            finishTest();
            return;
        }
        
        QuizQuestion question = testQuestions.get(index);
        tvQuestion.setText(question.getQuestion());
        tvProgress.setText("問題 " + (index + 1) + "/" + testQuestions.size());
        
        List<String> options = question.getOptions();
        for (int i = 0; i < optionButtons.length; i++) {
            if (i < options.size()) {
                optionButtons[i].setText(options.get(i));
                optionButtons[i].setVisibility(android.view.View.VISIBLE);
            } else {
                optionButtons[i].setVisibility(android.view.View.GONE);
            }
        }
        
        resetButtons();
    }
    
    private void selectAnswer(int index) {
        QuizQuestion question = testQuestions.get(currentQuestionIndex);
        boolean isCorrect = question.checkAnswer(index);
        
        if (isCorrect) {
            testScore += 5; // テストは1問5点
        }
        
        highlightAnswer(index, isCorrect);
        btnNext.setEnabled(true);
    }
    
    private void highlightAnswer(int selectedIndex, boolean isCorrect) {
        QuizQuestion question = testQuestions.get(currentQuestionIndex);
        int correctIndex = question.getCorrectIndex();
        
        if (isCorrect) {
            optionButtons[selectedIndex].setBackgroundColor(getColor(R.color.correct_answer));
        } else {
            optionButtons[selectedIndex].setBackgroundColor(getColor(R.color.wrong_answer));
            optionButtons[correctIndex].setBackgroundColor(getColor(R.color.correct_answer));
        }
    }
    
    private void resetButtons() {
        for (Button button : optionButtons) {
            button.setBackgroundColor(getColor(R.color.button_normal));
            button.setEnabled(true);
        }
        btnNext.setEnabled(false);
    }
    
    private void nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < testQuestions.size()) {
            displayQuestion(currentQuestionIndex);
        } else {
            finishTest();
        }
    }
    
    private void finishTest() {
        // 結果を表示
        TestResultDialog dialog = new TestResultDialog(this, testScore, testQuestions.size() * 5);
        dialog.show();
    }
}