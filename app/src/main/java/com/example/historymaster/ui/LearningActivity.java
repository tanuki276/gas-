package com.example.historymaster.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.historymaster.R;
import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.models.HistoryData;
import com.example.historymaster.models.Period;
import com.example.historymaster.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class LearningActivity extends AppCompatActivity {
    private TextView tvProgress;
    private HistoryData historyData;
    private String region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        region = JsonLoader.normalizeRegion(getIntent().getStringExtra("region"));
        historyData = JsonLoader.loadHistoryData(this, JsonLoader.getRegionFileName(region));
        initUI();
        updateProgressText();
        if (historyData != null) setupLearningTopics();
        else Toast.makeText(this, "歴史データの読み込みに失敗しました", Toast.LENGTH_LONG).show();
    }

    private void initUI() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvProgress = findViewById(R.id.tv_progress);
        Button btnStartSession = findViewById(R.id.btn_start_session);
        btnStartSession.setOnClickListener(v -> startLearningSession());
    }

    private void setupLearningTopics() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new TopicAdapter(historyData.getPeriods()));
    }

    private void startLearningSession() {
        if (historyData == null || historyData.getPeriods() == null || historyData.getPeriods().isEmpty()) return;
        int current = SharedPrefsHelper.getLearningProgress(this, region);
        int next = Math.min(100, current + 10);
        SharedPrefsHelper.saveLearningProgress(this, region, next);
        updateProgressText();
        new AlertDialog.Builder(this)
            .setTitle("学習セッション完了")
            .setMessage("この地域の学習進捗を " + next + "% に更新しました。")
            .setPositiveButton("OK", null)
            .show();
    }

    private void updateProgressText() {
        int progress = SharedPrefsHelper.getLearningProgress(this, region);
        tvProgress.setText("学習進捗: " + progress + "%");
    }

    private static class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.Holder> {
        private final List<Period> items;
        TopicAdapter(List<Period> periods) { items = periods == null ? new ArrayList<>() : periods; }

        @NonNull @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
            Period period = items.get(position);
            holder.title.setText(period.getUpperLevel() == null ? "" : period.getUpperLevel());
            int count = period.getMiddleLevelEntities() == null ? 0 : period.getMiddleLevelEntities().size();
            holder.subtitle.setText("主要項目: " + count + "件");
        }

        @Override public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView title, subtitle;
            Holder(View view) {
                super(view);
                title = view.findViewById(android.R.id.text1);
                subtitle = view.findViewById(android.R.id.text2);
            }
        }
    }
}
