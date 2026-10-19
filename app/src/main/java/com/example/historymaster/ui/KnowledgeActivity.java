package com.example.historymaster.ui;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.models.Entity;
import com.example.historymaster.models.Period;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class KnowledgeActivity extends AppCompatActivity {
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

        addText("人物・戦争・条約", 24);
        addText("現行の歴史JSONの名称・タグから自動抽出する索引です。データ拡充に合わせて自動で増えます。", 14);
        String region = JsonLoader.normalizeRegion(getIntent().getStringExtra("region"));
        JsonLoader.loadHistoryDataAsync(this, JsonLoader.getRegionFileName(region), data -> {
            if (data == null || data.getPeriods() == null) {
                addText("データを読み込めませんでした。", 16);
                return;
            }
            List<Entity> all = new ArrayList<>();
            for (Period period : data.getPeriods()) collect(period.getMiddleLevelEntities(), all);
            addCategory("人物・指導者", all, Arrays.asList("皇帝", "王", "将軍", "摂政", "首相", "建国", "太祖", "天皇"));
            addCategory("戦争・反乱・政変", all, Arrays.asList("戦争", "戦い", "乱", "反乱", "元寇", "侵攻", "内戦", "クーデター", "蜂起"));
            addCategory("条約・外交・従属関係", all, Arrays.asList("条約", "同盟", "講和", "朝貢", "外交", "保護国", "属領", "従属", "宗主権"));
        });
    }

    private void addCategory(String title, List<Entity> entities, List<String> keywords) {
        addText("\n" + title, 20);
        int count = 0;
        for (Entity entity : entities) {
            String haystack = searchable(entity);
            boolean matches = false;
            for (String keyword : keywords) {
                if (haystack.contains(keyword.toLowerCase(Locale.ROOT))) {
                    matches = true;
                    break;
                }
            }
            if (!matches) continue;
            count++;
            String tags = entity.getTags() == null ? "" : android.text.TextUtils.join("・", entity.getTags());
            addText("• " + entity.getName() + (tags.isEmpty() ? "" : " — " + tags), 14);
        }
        if (count == 0) addText("該当データはまだありません。", 14);
    }

    private String searchable(Entity entity) {
        StringBuilder builder = new StringBuilder();
        if (entity.getName() != null) builder.append(entity.getName()).append(' ');
        if (entity.getTags() != null) {
            for (String tag : entity.getTags()) if (tag != null) builder.append(tag).append(' ');
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private void collect(List<Entity> entities, List<Entity> out) {
        if (entities == null) return;
        for (Entity entity : entities) {
            if (entity == null) continue;
            out.add(entity);
            collect(entity.getSubEntities(), out);
        }
    }

    private void addText(String text, int sp) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sp);
        view.setPadding(0, dp(4), 0, dp(4));
        content.addView(view);
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
