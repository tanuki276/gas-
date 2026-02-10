package com.historymaster.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.historymaster.R;
import com.historymaster.data.JsonLoader;
import com.historymaster.data.QuizGenerator;
import com.historymaster.models.HistoryData;
import com.historymaster.models.QuizQuestion;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer timer;
    
    private TextView tvQuestion, tvQuestionNumber, tvTimer, tvScore;
    private RadioGroup radioGroup;
    private Button btnSubmit, btnNext;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        
        String region = getIntent().getStringExtra("region");
        
        // データ読み込みとクイズ生成
        HistoryData data = JsonLoader.loadHistoryData(this,
            JsonLoader.getRegionFileName(region));
        
        if (data != null) {
            questions = QuizGenerator.generateQuiz(data, 10);
        }
        
        initUI();
        
        if (questions != null && !questions.isEmpty()) {
            displayQuestion(currentQuestionIndex);
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
        
        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> nextQuestion());
        
        startTimer();
    }
    
    private void displayQuestion(int index) {
        if (index >= questions.size()) {
            finishQuiz();
            return;
        }
        
        QuizQuestion question = questions.get(index);
        tvQuestion.setText(question.getQuestion());
        tvQuestionNumber.setText("問題 " + (index + 1) + "/" + questions.size());
        
        // 選択肢を表示
        radioGroup.removeAllViews();
        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(options.get(i));
            radioButton.setId(i);
            radioGroup.addView(radioButton);
        }
        
        // ボタン状態をリセット
        btnSubmit.setEnabled(true);
        btnNext.setEnabled(false);
    }
    
    private void startTimer() {
        if (timer != null) timer.cancel();
        
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText(seconds + "秒");
            }
            
            @Override
            public void onFinish() {
                // 時間切れ
                checkAnswer();
            }
        }.start();
    }
    
    private void checkAnswer() {
        timer.cancel();
        
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            // 回答がない場合は不正解扱い
            showAnswerFeedback(false);
            return;
        }
        
        QuizQuestion currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = currentQuestion.checkAnswer(selectedId);
        
        if (isCorrect) {
            score += 10;
            tvScore.setText("スコア: " + score);
        }
        
        showAnswerFeedback(isCorrect);
        btnSubmit.setEnabled(false);
        btnNext.setEnabled(true);
    }
    
    private void showAnswerFeedback(boolean isCorrect) {
        if (isCorrect) {
            // 正解のアニメーションやメッセージ
        } else {
            // 不正解のメッセージ
            QuizQuestion question = questions.get(currentQuestionIndex);
            int correctIndex = question.getCorrectIndex();
            RadioButton correctButton = findViewById(correctIndex);
            if (correctButton != null) {
                correctButton.setTextColor(getColor(R.color.correct_answer));
            }
        }
    }
    
    private void nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion(currentQuestionIndex);
            startTimer();
        } else {
            finishQuiz();
        }
    }
    
    private void finishQuiz() {
        // 結果を表示
        QuizResultDialog dialog = new QuizResultDialog(this, score, questions.size() * 10);
        dialog.show();
        
        // ユーザープログレスを保存
        saveQuizResult();
    }
    
    private void saveQuizResult() {
        // SharedPreferencesに結果を保存
    }
}