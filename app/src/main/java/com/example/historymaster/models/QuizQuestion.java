package com.example.historymaster.models;

import java.util.List;

public class QuizQuestion {
    private String question;
    private List<String> options;
    private int correctIndex;
    private String type;
    private String explanation;
    
    public QuizQuestion(String question, List<String> options, int correctIndex, String type) {
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
        this.type = type;
    }
    
    // ゲッター
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public String getType() { return type; }
    public String getExplanation() { return explanation; }
    
    // セッター
    public void setExplanation(String explanation) { this.explanation = explanation; }
    
    // 正解チェック
    public boolean checkAnswer(int selectedIndex) {
        return selectedIndex == correctIndex;
    }
}