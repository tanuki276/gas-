package com.example.historymaster.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.historymaster.R;
import com.example.historymaster.data.JsonLoader;
import com.example.historymaster.models.Entity;
import com.example.historymaster.models.HistoryData;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    
    private EditText etSearch;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private HistoryData historyData;
    private List<Entity> allEntities = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        String region = getIntent().getStringExtra("region");
        
        historyData = JsonLoader.loadHistoryData(this,
            JsonLoader.getRegionFileName(region));
        
        initUI();
        
        if (historyData != null) {
            loadAllEntities();
        }
    }
    
    private void initUI() {
        etSearch = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new SearchAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        
        // 検索リスナー
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadAllEntities() {
        if (historyData.getPeriods() != null) {
            for (Period period : historyData.getPeriods()) {
                collectEntities(period.getMiddleLevelEntities(), allEntities);
            }
        }
    }
    
    private void collectEntities(List<Entity> entities, List<Entity> result) {
        for (Entity entity : entities) {
            result.add(entity);
            if (entity.getSubEntities() != null) {
                collectEntities(entity.getSubEntities(), result);
            }
        }
    }
    
    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            adapter.updateData(new ArrayList<>());
            return;
        }
        
        List<Entity> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Entity entity : allEntities) {
            if (entity.getName().toLowerCase().contains(lowerQuery)) {
                results.add(entity);
                continue;
            }
            
            if (entity.getTags() != null) {
                for (String tag : entity.getTags()) {
                    if (tag.toLowerCase().contains(lowerQuery)) {
                        results.add(entity);
                        break;
                    }
                }
            }
        }
        
        adapter.updateData(results);
    }
}