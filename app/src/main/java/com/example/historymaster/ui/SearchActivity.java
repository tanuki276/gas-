package com.example.historymaster.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    private EditText etSearch;
    private SearchAdapter adapter;
    private final List<Entity> allEntities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        String region = JsonLoader.normalizeRegion(getIntent().getStringExtra("region"));
        HistoryData historyData = JsonLoader.loadHistoryData(this, JsonLoader.getRegionFileName(region));
        initUI();

        if (historyData != null && historyData.getPeriods() != null) {
            for (Period period : historyData.getPeriods()) {
                if (period != null) collectEntities(period.getMiddleLevelEntities(), allEntities);
            }
        } else {
            Toast.makeText(this, "歴史データの読み込みに失敗しました", Toast.LENGTH_LONG).show();
        }
    }

    private void initUI() {
        etSearch = findViewById(R.id.et_search);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter();
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s == null ? "" : s.toString());
            }
        });
    }

    private void collectEntities(List<Entity> entities, List<Entity> result) {
        if (entities == null) return;
        for (Entity entity : entities) {
            if (entity == null) continue;
            result.add(entity);
            collectEntities(entity.getSubEntities(), result);
        }
    }

    private void performSearch(String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            adapter.updateData(new ArrayList<>());
            return;
        }

        List<Entity> results = new ArrayList<>();
        for (Entity entity : allEntities) {
            if (matches(entity, normalized)) results.add(entity);
        }
        adapter.updateData(results);
    }

    private boolean matches(Entity entity, String query) {
        if (entity.getName() != null && entity.getName().toLowerCase(Locale.ROOT).contains(query)) return true;
        if (entity.getTags() != null) {
            for (String tag : entity.getTags()) {
                if (tag != null && tag.toLowerCase(Locale.ROOT).contains(query)) return true;
            }
        }
        return false;
    }

    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.Holder> {
        private List<Entity> items = new ArrayList<>();

        void updateData(List<Entity> data) {
            items = data == null ? new ArrayList<>() : data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Entity entity = items.get(position);
            holder.title.setText(entity.getName() == null ? "" : entity.getName());
            if (entity.getTags() == null || entity.getTags().isEmpty()) {
                holder.subtitle.setText("説明なし");
            } else {
                holder.subtitle.setText(android.text.TextUtils.join("・", entity.getTags()));
            }
        }

        @Override public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView subtitle;
            Holder(View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
