package com.example.historymaster;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.example.historymaster.ui.*;
import com.example.historymaster.data.*;
import com.example.historymaster.utils.SharedPrefsHelper;

public class MainActivity extends Screen {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        region = Regions.safe(SharedPrefsHelper.getCurrentRegion(this, "china"));
    }

    @Override protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        int current = Regions.index(region);
        page("歴史を、旅しよう。", "HISTORY MASTER  /  アジアの7地域を探究");
        text("探索する地域", 16, true);
        Spinner picker = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, Regions.NAMES);
        picker.setAdapter(adapter);
        picker.setSelection(current);
        picker.setMinimumHeight(dp(56));
        picker.setContentDescription("探索する地域を選択");
        body.addView(picker);
        picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!region.equals(Regions.IDS[position])) {
                    region = Regions.IDS[position];
                    SharedPrefsHelper.saveCurrentRegion(MainActivity.this, region);
                    render();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        text(Regions.NAMES[current] + "を知る旅へ", 26, true);
        text("解説をひとつ読む。それだけでも、新しい発見につながります。", 15, false);
        button("はじめに読む → 地域の歴史", () -> article(Regions.ARTICLES[current]), true);
        button("時代・国・王朝から探索", () -> open(ExplanationActivity.class), false);
        button("キーワードで検索", () -> open(SearchActivity.class), false);

        text("あなたの学習記録", 22, true);
        text("既読 " + SharedPrefsHelper.readCount(this, region)
                + "項目  ·  クイズ最高 " + SharedPrefsHelper.getQuizScore(this, region) + "点", 16, false);
        button("学習リストを開く", () -> open(LearningActivity.class), false);

        text("覚えたことを、試そう。", 22, true);
        text("時間制限はありません。回答後に関連語を確認できます。", 15, false);
        button("5問のクイズに挑戦", () -> open(QuizActivity.class), true);
        button("理解度テスト · 10問", () -> open(TestActivity.class), false);
    }
}
