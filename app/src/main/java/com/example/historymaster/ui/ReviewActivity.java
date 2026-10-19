package com.example.historymaster.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.historymaster.models.ReviewItem;
import com.example.historymaster.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {
    private final List<ReviewItem> items = new ArrayList<>();
    private int index = 0;
    private TextView progress;
    private TextView prompt;
    private TextView answer;
    private Button reveal;
    private Button again;
    private Button knew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
        items.addAll(SharedPrefsHelper.getDueReviewItems(this));
        render();
    }

    private View buildContent() {
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = text("復習トレーニング", 24);
        root.addView(title);
        progress = text("", 14);
        root.addView(progress);
        prompt = text("", 20);
        prompt.setPadding(0, dp(32), 0, dp(24));
        root.addView(prompt);
        answer = text("", 17);
        answer.setVisibility(View.GONE);
        root.addView(answer);

        reveal = new Button(this);
        reveal.setText("答えを見る");
        reveal.setOnClickListener(v -> {
            answer.setVisibility(View.VISIBLE);
            reveal.setVisibility(View.GONE);
            again.setVisibility(View.VISIBLE);
            knew.setVisibility(View.VISIBLE);
        });
        root.addView(reveal);

        again = new Button(this);
        again.setText("もう一度");
        again.setVisibility(View.GONE);
        again.setOnClickListener(v -> grade(false));
        root.addView(again);

        knew = new Button(this);
        knew.setText("覚えた  +5 XP");
        knew.setVisibility(View.GONE);
        knew.setOnClickListener(v -> grade(true));
        root.addView(knew);
        return root;
    }

    private void render() {
        if (items.isEmpty() || index >= items.size()) {
            progress.setText("復習待ち 0件");
            prompt.setText("今日の復習は完了です 🎉");
            answer.setVisibility(View.VISIBLE);
            answer.setText("間違えた問題は自動でここに追加されます。");
            reveal.setVisibility(View.GONE);
            again.setVisibility(View.GONE);
            knew.setVisibility(View.GONE);
            return;
        }
        ReviewItem item = items.get(index);
        progress.setText("復習 " + (index + 1) + " / " + items.size());
        prompt.setText(item.getPrompt());
        answer.setText(item.getAnswer());
        answer.setVisibility(View.GONE);
        reveal.setVisibility(View.VISIBLE);
        again.setVisibility(View.GONE);
        knew.setVisibility(View.GONE);
    }

    private void grade(boolean correct) {
        ReviewItem item = items.get(index);
        if (correct) SharedPrefsHelper.markReviewCorrect(this, item);
        else SharedPrefsHelper.markReviewWrong(this, item);
        index++;
        render();
    }

    private TextView text(String value, int sp) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
