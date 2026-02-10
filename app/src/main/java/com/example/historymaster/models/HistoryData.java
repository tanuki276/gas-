package com.historymaster.models;

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

class Period {
    private String upper_level;
    private List<Entity> middle_level_entities;
    
    public String getUpperLevel() { return upper_level; }
    public void setUpperLevel(String upper_level) { this.upper_level = upper_level; }
    
    public List<Entity> getMiddleLevelEntities() { return middle_level_entities; }
    public void setMiddleLevelEntities(List<Entity> middle_level_entities) {
        this.middle_level_entities = middle_level_entities;
    }
}

class Entity {
    private String name;
    private List<String> tags;
    private List<Entity> sub_entities;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public List<Entity> getSubEntities() { return sub_entities; }
    public void setSubEntities(List<Entity> sub_entities) {
        this.sub_entities = sub_entities;
    }
}