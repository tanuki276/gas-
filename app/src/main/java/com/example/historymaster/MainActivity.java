package com.example.historymaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private String currentRegion = "china";
    private TextView tvWelcome;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // UI初期化
        initViews();
        
        // ユーザー進捗を読み込み
        loadUserProgress();
    }
    
    private void initViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        
        // 地域選択ボタン
        Button btnChina = findViewById(R.id.btn_china);
        Button btnJapan = findViewById(R.id.btn_japan);
        Button btnKorea = findViewById(R.id.btn_korea);
        Button btnMongol = findViewById(R.id.btn_mongol);
        Button btnVietnam = findViewById(R.id.btn_vietnam);
        Button btnCambodia = findViewById(R.id.btn_cambodia);
        
        // 地域ボタンのリスナー
        btnChina.setOnClickListener(v -> setRegion("china"));
        btnJapan.setOnClickListener(v -> setRegion("japan"));
        btnKorea.setOnClickListener(v -> setRegion("korea"));
        btnMongol.setOnClickListener(v -> setRegion("mongol"));
        btnVietnam.setOnClickListener(v -> setRegion("vietnam"));
        btnCambodia.setOnClickListener(v -> setRegion("cambodia"));
        
        // 機能ボタン
        Button btnExplanation = findViewById(R.id.btn_explanation);
        Button btnQuiz = findViewById(R.id.btn_quiz);
        Button btnLearning = findViewById(R.id.btn_learning);
        Button btnSearch = findViewById(R.id.btn_search);
        Button btnTest = findViewById(R.id.btn_test);
        
        btnExplanation.setOnClickListener(v -> openExplanation());
        btnQuiz.setOnClickListener(v -> openQuiz());
        btnLearning.setOnClickListener(v -> openLearning());
        btnSearch.setOnClickListener(v -> openSearch());
        btnTest.setOnClickListener(v -> openTest());
    }
    
    private void setRegion(String region) {
        currentRegion = region;
        SharedPrefsHelper.saveCurrentRegion(this, region);
        
        // 地域名を表示
        String regionName = getRegionDisplayName(region);
        tvWelcome.setText(regionName + "の歴史を学ぼう！");
    }
    
    private String getRegionDisplayName(String regionCode) {
        switch (regionCode) {
            case "china": return "中国";
            case "japan": return "日本";
            case "korea": return "韓国";
            case "mongol": return "モンゴル";
            case "vietnam": return "ベトナム";
            case "cambodia": return "カンボジア";
            default: return "中国";
        }
    }
    
    private void openExplanation() {
        Intent intent = new Intent(this, ExplanationActivity.class);
        intent.putExtra("region", currentRegion);
        startActivity(intent);
    }
    
    private void openQuiz() {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("region", currentRegion);
        startActivity(intent);
    }
    
    private void openLearning() {
        Intent intent = new Intent(this, LearningActivity.class);
        intent.putExtra("region", currentRegion);
        startActivity(intent);
    }
    
    private void openSearch() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("region", currentRegion);
        startActivity(intent);
    }
    
    private void openTest() {
        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra("region", currentRegion);
        startActivity(intent);
    }
    
    private void loadUserProgress() {
        // 現在の地域を読み込み
        currentRegion = SharedPrefsHelper.getCurrentRegion(this, "china");
        setRegion(currentRegion);
    }
}