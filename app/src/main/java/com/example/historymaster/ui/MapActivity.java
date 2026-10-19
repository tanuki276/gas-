package com.example.historymaster.ui;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.models.Entity;
import com.example.historymaster.models.Period;

import java.util.List;

public class MapActivity extends AppCompatActivity {
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        content.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        setContentView(scroll);

        addText("勢力図 β", 24);
        addText("※ 初版は実国境を描く地図ではなく、各時代に存在した主要勢力の概略表示です。誤った国境表現を避けつつ、今後GIS/境界データへ拡張できる構成にしています。", 14);
        String region = JsonLoader.normalizeRegion(getIntent().getStringExtra("region"));
        JsonLoader.loadHistoryDataAsync(this, JsonLoader.getRegionFileName(region), data -> {
            if (data == null || data.getPeriods() == null) {
                addText("データを読み込めませんでした。", 16);
                return;
            }
            for (Period period : data.getPeriods()) {
                addText("\n■ " + safe(period.getUpperLevel()), 19);
                appendTopLevel(period.getMiddleLevelEntities());
            }
        });
    }

    private void appendTopLevel(List<Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            addText("主要勢力データなし", 14);
            return;
        }
        StringBuilder line = new StringBuilder();
        for (Entity entity : entities) {
            if (entity == null || entity.getName() == null) continue;
            if (line.length() > 0) line.append("  /  ");
            line.append(entity.getName());
        }
        addText(line.toString(), 15);
    }

    private void addText(String text, int sp) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sp);
        view.setPadding(0, dp(5), 0, dp(5));
        content.addView(view);
    }

    private String safe(String value) { return value == null ? "" : value; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
