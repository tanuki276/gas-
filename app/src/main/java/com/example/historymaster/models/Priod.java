// app/src/main/java/com/historymaster/models/Period.java
package com.historymaster.models;

import java.util.List;

public class Period {
    private String upper_level;
    private List<Entity> middle_level_entities;
    
    public Period() {}
    
    // ゲッター
    public String getUpperLevel() { return upper_level; }
    public List<Entity> getMiddleLevelEntities() { return middle_level_entities; }
    
    // セッター
    public void setUpperLevel(String upper_level) { this.upper_level = upper_level; }
    public void setMiddleLevelEntities(List<Entity> middle_level_entities) {
        this.middle_level_entities = middle_level_entities;
    }
}