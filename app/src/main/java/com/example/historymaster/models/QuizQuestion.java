package com.example.historymaster.models;

import java.util.List;

public class QuizQuestion {
  private String question, type, explanation, id, region, sourceTitle;
  private List<String> options;
  private int correctIndex;

  public QuizQuestion(String question, List<String> options, int correctIndex, String type) {
    this.question = question;
    this.options = options;
    this.correctIndex = correctIndex;
    this.type = type;
  }

  public String getQuestion() {
    return question;
  }

  public List<String> getOptions() {
    return options;
  }

  public int getCorrectIndex() {
    return correctIndex;
  }

  public String getType() {
    return type;
  }

  public String getExplanation() {
    return explanation;
  }

  public String getId() {
    return id == null ? question : id;
  }

  public String getRegion() {
    return region == null ? "china" : region;
  }

  public String getSourceTitle() {
    return sourceTitle;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  public void setIdentity(String id, String region, String sourceTitle) {
    this.id = id;
    this.region = region;
    this.sourceTitle = sourceTitle;
  }

  public boolean checkAnswer(int selectedIndex) {
    return selectedIndex == correctIndex;
  }
}
