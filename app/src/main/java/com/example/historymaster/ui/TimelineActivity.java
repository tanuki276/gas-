package com.example.historymaster.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.models.Entity;
import com.example.historymaster.models.Period;

import java.util.List;

public class TimelineActivity extends AppCompatActivity {
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

        addText("歴史タイムライン", 24);
        addText("王朝・国家の流れを時代順に確認できます。", 14);
        String region = JsonLoader.normalizeRegion(getIntent().getStringExtra("region"));
        JsonLoader.loadHistoryDataAsync(this, JsonLoader.getRegionFileName(region), data -> {
            if (data == null || data.getPeriods() == null) {
                addText("データを読み込めませんでした。", 16);
                return;
            }
            for (Period period : data.getPeriods()) {
                addText("\n" + safe(period.getUpperLevel()), 20);
                appendEntities(period.getMiddleLevelEntities(), 0);
            }
        });
    }

    private void appendEntities(List<Entity> entities, int depth) {
        if (entities == null) return;
        for (Entity entity : entities) {
            if (entity == null || entity.getName() == null) continue;
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < depth; i++) line.append("　");
            line.append("• ").append(entity.getName());
            if (entity.getTags() != null && !entity.getTags().isEmpty()) {
                line.append("  — ").append(entity.getTags().get(0));
            }
            addText(line.toString(), 15);
            appendEntities(entity.getSubEntities(), depth + 1);
        }
    }

    private TextView addText(String text, int sp) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sp);
        view.setPadding(0, dp(4), 0, dp(4));
        content.addView(view);
        return view;
    }

    private String safe(String value) { return value == null ? "" : value; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
