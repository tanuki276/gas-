package com.historymaster.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.historymaster.R;
import com.historymaster.data.JsonLoader;
import com.historymaster.models.HistoryData;
import com.historymaster.models.Period;
import java.util.List;

public class ExplanationActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private TextView tvRegionName;
    private HistoryData historyData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explanation);
        
        String region = getIntent().getStringExtra("region");
        
        // データ読み込み
        historyData = JsonLoader.loadHistoryData(this, 
            JsonLoader.getRegionFileName(region));
        
        initUI();
        
        if (historyData != null) {
            displayData();
        }
    }
    
    private void initUI() {
        tvRegionName = findViewById(R.id.tv_region_name);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void displayData() {
        tvRegionName.setText(historyData.getRegionName());
        
        // アダプター設定
        PeriodAdapter adapter = new PeriodAdapter(historyData.getPeriods());
        recyclerView.setAdapter(adapter);
    }
    
    // アダプタークラス
    class PeriodAdapter extends RecyclerView.Adapter<PeriodAdapter.ViewHolder> {
        private List<Period> periods;
        
        public PeriodAdapter(List<Period> periods) {
            this.periods = periods;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_period, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Period period = periods.get(position);
            holder.tvPeriodName.setText(period.getUpperLevel());
            
            // エンティティを表示
            StringBuilder entitiesText = new StringBuilder();
            for (Entity entity : period.getMiddleLevelEntities()) {
                entitiesText.append("・").append(entity.getName()).append("\n");
            }
            holder.tvEntities.setText(entitiesText.toString());
        }
        
        @Override
        public int getItemCount() {
            return periods.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPeriodName;
            TextView tvEntities;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvPeriodName = itemView.findViewById(R.id.tv_period_name);
                tvEntities = itemView.findViewById(R.id.tv_entities);
            }
        }
    }
}