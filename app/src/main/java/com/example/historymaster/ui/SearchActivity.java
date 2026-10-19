package com.example.historymaster.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.historymaster.R;
import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.models.Entity;
import com.example.historymaster.models.HistoryData;
import com.example.historymaster.models.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    private static final long SEARCH_DEBOUNCE_MS = 180L;

    private EditText etSearch;
    private SearchAdapter adapter;
    private final List<SearchEntry> searchEntries = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = () -> performSearch(etSearch == null ? "" : etSearch.getText().toString());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initUI();

        String region = JsonLoader.normalizeRegion(getIntent().getStringExtra("region"));
        JsonLoader.loadHistoryDataAsync(this, JsonLoader.getRegionFileName(region), data -> {
            if (isFinishing() || isDestroyed()) return;
            if (data == null || data.getPeriods() == null) {
                Toast.makeText(this, "歴史データの読み込みに失敗しました", Toast.LENGTH_LONG).show();
                return;
            }
            buildSearchIndex(data);
            performSearch(etSearch.getText().toString());
        });
    }

    private void initUI() {
        etSearch = findViewById(R.id.et_search);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new SearchAdapter();
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
    }

    private void buildSearchIndex(HistoryData historyData) {
        searchEntries.clear();
        for (Period period : historyData.getPeriods()) {
            if (period != null) collectEntities(period.getMiddleLevelEntities());
        }
    }

    private void collectEntities(List<Entity> entities) {
        if (entities == null) return;
        for (Entity entity : entities) {
            if (entity == null) continue;
            String name = entity.getName() == null ? "" : entity.getName();
            String tags = entity.getTags() == null ? "" : TextUtils.join("・", entity.getTags());
            String searchable = (name + " " + tags).toLowerCase(Locale.ROOT);
            searchEntries.add(new SearchEntry(entity, searchable));
            collectEntities(entity.getSubEntities());
        }
    }

    private void performSearch(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            adapter.updateData(Collections.emptyList());
            return;
        }

        List<Entity> results = new ArrayList<>();
        for (SearchEntry entry : searchEntries) {
            if (entry.searchableText.contains(normalized)) results.add(entry.entity);
        }
        adapter.updateData(results);
    }

    @Override
    protected void onDestroy() {
        searchHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private static class SearchEntry {
        final Entity entity;
        final String searchableText;
        SearchEntry(Entity entity, String searchableText) {
            this.entity = entity;
            this.searchableText = searchableText;
        }
    }

    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.Holder> {
        private List<Entity> items = Collections.emptyList();

        void updateData(List<Entity> data) {
            List<Entity> next = data == null ? Collections.emptyList() : new ArrayList<>(data);
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override public int getOldListSize() { return items.size(); }
                @Override public int getNewListSize() { return next.size(); }
                @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    String oldName = items.get(oldItemPosition).getName();
                    String newName = next.get(newItemPosition).getName();
                    return oldName != null && oldName.equals(newName);
                }
                @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Entity oldItem = items.get(oldItemPosition);
                    Entity newItem = next.get(newItemPosition);
                    return java.util.Objects.equals(oldItem.getName(), newItem.getName())
                        && java.util.Objects.equals(oldItem.getTags(), newItem.getTags());
                }
            });
            items = next;
            diff.dispatchUpdatesTo(this);
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
            holder.subtitle.setText(entity.getTags() == null || entity.getTags().isEmpty()
                ? "説明なし" : TextUtils.join("・", entity.getTags()));
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
