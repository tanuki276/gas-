package com.example.historymaster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.ui.ExplanationActivity;
import com.example.historymaster.ui.LearningActivity;
import com.example.historymaster.ui.QuizActivity;
import com.example.historymaster.ui.SearchActivity;
import com.example.historymaster.ui.TestActivity;
import com.example.historymaster.utils.SharedPrefsHelper;

public class MainActivity extends AppCompatActivity {

    private String currentRegion = "china";
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        loadUserProgress();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tv_welcome);

        findViewById(R.id.btn_china).setOnClickListener(v -> setRegion("china"));
        findViewById(R.id.btn_japan).setOnClickListener(v -> setRegion("japan"));
        findViewById(R.id.btn_korea).setOnClickListener(v -> setRegion("korea"));
        findViewById(R.id.btn_mongol).setOnClickListener(v -> setRegion("mongol"));
        findViewById(R.id.btn_vietnam).setOnClickListener(v -> setRegion("vietnam"));
        findViewById(R.id.btn_cambodia).setOnClickListener(v -> setRegion("cambodia"));

        findViewById(R.id.btn_explanation).setOnClickListener(v -> openActivity(ExplanationActivity.class));
        findViewById(R.id.btn_quiz).setOnClickListener(v -> openActivity(QuizActivity.class));
        findViewById(R.id.btn_learning).setOnClickListener(v -> openActivity(LearningActivity.class));
        findViewById(R.id.btn_search).setOnClickListener(v -> openActivity(SearchActivity.class));
        findViewById(R.id.btn_test).setOnClickListener(v -> openActivity(TestActivity.class));
    }

    private void setRegion(String region) {
        String normalized = JsonLoader.normalizeRegion(region);
        if (!normalized.equals(currentRegion)) {
            currentRegion = normalized;
            SharedPrefsHelper.saveCurrentRegion(this, normalized);
        }
        tvWelcome.setText(getRegionDisplayName(normalized) + "の歴史を学ぼう！");
        JsonLoader.preloadRegion(this, normalized);
    }

    private String getRegionDisplayName(String regionCode) {
        switch (regionCode) {
            case "china": return "中国";
            case "japan": return "日本";
            case "korea": return "韓国";
            case "mongol": return "モンゴル";
            case "vietnam": return "ベトナム";
            case "cambodia": return "カンボジア";
            case "laos": return "ラオス";
            default: return "中国";
        }
    }

    private void openActivity(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.putExtra("region", currentRegion);
        startActivity(intent);
    }

    private void loadUserProgress() {
        currentRegion = JsonLoader.normalizeRegion(
            SharedPrefsHelper.getCurrentRegion(this, "china"));
        tvWelcome.setText(getRegionDisplayName(currentRegion) + "の歴史を学ぼう！");
        JsonLoader.preloadRegion(this, currentRegion);
    }
}
