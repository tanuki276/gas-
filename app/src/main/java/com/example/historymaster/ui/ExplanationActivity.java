package com.example.historymaster.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.historymaster.R;
import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.models.Entity;
import com.example.historymaster.models.HistoryData;
import com.example.historymaster.models.Period;

import java.util.ArrayList;
import java.util.List;

public class ExplanationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvRegionName;
    private HistoryData historyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explanation);

        String region = JsonLoader.normalizeRegion(getIntent().getStringExtra("region"));
        historyData = JsonLoader.loadHistoryData(this, JsonLoader.getRegionFileName(region));
        initUI();

        if (historyData != null) {
            displayData();
        } else {
            tvRegionName.setText("データを読み込めませんでした");
            Toast.makeText(this, "歴史データの読み込みに失敗しました", Toast.LENGTH_LONG).show();
        }
    }

    private void initUI() {
        tvRegionName = findViewById(R.id.tv_region_name);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void displayData() {
        tvRegionName.setText(historyData.getRegionName() == null ? "歴史解説" : historyData.getRegionName());
        recyclerView.setAdapter(new PeriodAdapter(historyData.getPeriods()));
    }

    class PeriodAdapter extends RecyclerView.Adapter<PeriodAdapter.ViewHolder> {
        private final List<Period> periods;

        PeriodAdapter(List<Period> periods) {
            this.periods = periods == null ? new ArrayList<>() : periods;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_period, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Period period = periods.get(position);
            holder.tvPeriodName.setText(period.getUpperLevel() == null ? "" : period.getUpperLevel());
            StringBuilder text = new StringBuilder();
            appendEntities(period.getMiddleLevelEntities(), text, 0);
            holder.tvEntities.setText(text.toString().trim());
        }

        private void appendEntities(List<Entity> entities, StringBuilder out, int depth) {
            if (entities == null) return;
            for (Entity entity : entities) {
                if (entity == null || entity.getName() == null) continue;
                for (int i = 0; i < depth; i++) out.append("  ");
                out.append("・").append(entity.getName());
                if (entity.getTags() != null && !entity.getTags().isEmpty()) {
                    out.append(" — ").append(entity.getTags().get(0));
                }
                out.append('\n');
                appendEntities(entity.getSubEntities(), out, depth + 1);
            }
        }

        @Override
        public int getItemCount() {
            return periods.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvPeriodName;
            final TextView tvEntities;

            ViewHolder(View itemView) {
                super(itemView);
                tvPeriodName = itemView.findViewById(R.id.tv_period_name);
                tvEntities = itemView.findViewById(R.id.tv_entities);
            }
        }
    }
}
