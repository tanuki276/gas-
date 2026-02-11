// app/src/main/java/com/historymaster/models/Entity.java
package com.example.historymaster.models;

import java.util.List;

public class Entity {
    private String name;
    private List<String> tags;
    private List<Entity> sub_entities;
    
    public Entity() {}
    
    // ゲッター
    public String getName() { return name; }
    public List<String> getTags() { return tags; }
    public List<Entity> getSubEntities() { return sub_entities; }
    
    // セッター
    public void setName(String name) { this.name = name; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setSubEntities(List<Entity> sub_entities) { 
        this.sub_entities = sub_entities; 
    }
}