package com.example.historymaster.models;

import java.util.List;

public class HistoryData {
    private String region_name;
    private String description;
    private List<Period> periods;
    
    // コンストラクタ、ゲッター、セッター
    public HistoryData() {}
    
    public String getRegionName() { return region_name; }
    public void setRegionName(String region_name) { this.region_name = region_name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<Period> getPeriods() { return periods; }
    public void setPeriods(List<Period> periods) { this.periods = periods; }
}
