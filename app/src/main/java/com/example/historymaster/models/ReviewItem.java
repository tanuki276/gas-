package com.example.historymaster.models;

public class ReviewItem {
    private final String prompt;
    private final String answer;
    private final long dueAt;
    private final int intervalDays;

    public ReviewItem(String prompt, String answer, long dueAt, int intervalDays) {
        this.prompt = prompt;
        this.answer = answer;
        this.dueAt = dueAt;
        this.intervalDays = intervalDays;
    }

    public String getPrompt() { return prompt; }
    public String getAnswer() { return answer; }
    public long getDueAt() { return dueAt; }
    public int getIntervalDays() { return intervalDays; }
}
