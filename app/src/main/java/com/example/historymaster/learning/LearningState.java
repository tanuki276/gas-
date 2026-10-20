package com.example.historymaster.learning;

import java.util.*;

/** Pure Java rules; persisted by ProgressStore. Every reward is idempotent. */
public final class LearningState {
  public int xp, streak, longestStreak, sessions, perfectSessions;
  public int dailyXp, dailyLessons, dailyReads;
  public String activeDay = "";
  public Set<String> mastered = new HashSet<>();
  public Set<String> visitedRegions = new HashSet<>();
  public Set<String> events = new HashSet<>();
  public Map<String, Integer> best = new HashMap<>();

  public int award(String event, int amount, String day) {
    if (amount < 0 || events.contains(event)) return 0;
    events.add(event);
    advanceDay(day);
    xp += amount;
    if (day.equals(activeDay)) dailyXp += amount;
    return amount;
  }

  private void advanceDay(String day) {
    if (day.equals(activeDay)) return;
    long gap = activeDay.isEmpty() ? Long.MAX_VALUE : DayKey.distance(activeDay, day);
    if (gap < 0) return; // Do not manufacture a streak by moving the clock backwards.
    streak = gap == 1 ? streak + 1 : 1;
    longestStreak = Math.max(streak, longestStreak);
    activeDay = day;
    dailyXp = dailyLessons = dailyReads = 0;
  }

  public int complete(
      String session, String lesson, String region, int correct, int total, String day) {
    if (total <= 0 || correct < 0 || correct > total || events.contains("session:" + session))
      return 0;
    events.add("session:" + session);
    int percent = correct * 100 / total;
    best.put(lesson, Math.max(percent, best.containsKey(lesson) ? best.get(lesson) : 0));
    if (correct * 3 >= total * 2) mastered.add(lesson);
    sessions++;
    if (correct == total) perfectSessions++;
    if (!"review".equals(lesson)) visitedRegions.add(region);
    int earned = award("lesson:" + lesson + ":" + day, 20 + correct * 5, day);
    // First completion per lesson/day supplies one quest step, even when restored.
    if (earned > 0 && day.equals(activeDay)) dailyLessons++;
    return earned;
  }

  public int read(String key, String day) {
    int earned = award("read:" + key, 5, day);
    if (earned > 0 && day.equals(activeDay)) dailyReads++;
    return earned;
  }

  public int visibleStreak(String today) {
    return activeDay.isEmpty() || DayKey.distance(activeDay, today) > 1 ? 0 : streak;
  }

  public int xpToday(String today) {
    return today.equals(activeDay) ? dailyXp : 0;
  }

  public int level() {
    return 1 + xp / 100;
  }
}
