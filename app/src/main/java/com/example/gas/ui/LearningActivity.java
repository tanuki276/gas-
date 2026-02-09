package com.historymaster.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.historymaster.R;
import com.historymaster.data.JsonLoader;
import com.historymaster.models.HistoryData;
import com.historymaster.models.Period;

public class LearningActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private TextView tvProgress;
    private Button btnStartSession;
    private HistoryData historyData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        
        String region = getIntent().getStringExtra("region");
        
        historyData = JsonLoader.loadHistoryData(this,
            JsonLoader.getRegionFileName(region));
        
        initUI();
        
        if (historyData != null) {
            setupLearningTopics();
        }
    }
    
    private void initUI() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvProgress = findViewById(R.id.tv_progress);
        btnStartSession = findViewById(R.id.btn_start_session);
        
        btnStartSession.setOnClickListener(v -> startLearningSession());
    }
    
    private void setupLearningTopics() {
        LearningTopicAdapter adapter = new LearningTopicAdapter(historyData.getPeriods());
        recyclerView.setAdapter(adapter);
    }
    
    private void startLearningSession() {
        // 学習セッションを開始
        LearningSessionDialog dialog = new LearningSessionDialog(this, historyData);
        dialog.show();
    }
}