package com.example.historymaster.learning;

import static org.junit.Assert.*;

import org.junit.Test;

public class LearningStateTest {
  @Test
  public void duplicateSessionAndSameDayReplayNeverDoubleAward() {
    LearningState state = new LearningState();
    assertEquals(35, state.complete("one", "japan:nara", "japan", 3, 3, "2026-10-20"));
    assertEquals(0, state.complete("one", "japan:nara", "japan", 3, 3, "2026-10-20"));
    assertEquals(1, state.sessions);
    assertEquals(0, state.complete("two", "japan:nara", "japan", 2, 3, "2026-10-20"));
    assertEquals(35, state.xp);
    assertEquals(1, state.dailyLessons);
    assertEquals(100, (int) state.best.get("japan:nara"));
    assertEquals(30, state.complete("three", "japan:nara", "japan", 2, 3, "2026-10-21"));
    assertEquals(65, state.xp);
    assertEquals(2, state.streak);
  }

  @Test
  public void masteryRequiresTwoThirdsAndCanBeEarnedOnReplay() {
    LearningState state = new LearningState();
    state.complete("one", "lesson", "china", 1, 3, "2026-10-20");
    assertFalse(state.mastered.contains("lesson"));
    state.complete("two", "lesson", "china", 2, 3, "2026-10-20");
    assertTrue(state.mastered.contains("lesson"));
    assertEquals(25, state.xp);
  }

  @Test
  public void streakUsesCalendarDatesAndResetsAfterAGap() {
    LearningState state = new LearningState();
    state.award("a", 5, "2026-10-31");
    state.award("b", 5, "2026-11-01");
    assertEquals(2, state.streak);
    assertEquals(2, state.visibleStreak("2026-11-02"));
    assertEquals(0, state.visibleStreak("2026-11-03"));
    state.award("c", 5, "2026-11-03");
    assertEquals(1, state.streak);
    assertEquals(2, state.longestStreak);
    assertEquals(0, state.xpToday("2026-11-04"));
    assertEquals(1, DayKey.distance("2024-02-28", "2024-02-29"));
    assertEquals(1, DayKey.distance("2024-02-29", "2024-03-01"));
  }

  @Test
  public void readingAndRecreationAreIdempotent() {
    LearningState state = new LearningState();
    assertEquals(5, state.read("japan:江戸幕府", "2026-10-20"));
    assertEquals(0, state.read("japan:江戸幕府", "2026-10-21"));
    assertEquals(1, state.dailyReads);
    assertEquals(5, state.xp);
  }

  @Test
  public void invalidResultsAndBackwardsClockDoNotInventProgress() {
    LearningState state = new LearningState();
    assertEquals(0, state.complete("bad", "x", "japan", 4, 3, "2026-10-20"));
    assertEquals(0, state.complete("bad", "x", "japan", 0, 0, "2026-10-20"));
    state.award("now", 5, "2026-10-20");
    state.award("past", 5, "2026-10-19");
    assertEquals("2026-10-20", state.activeDay);
    assertEquals(1, state.streak);
    assertEquals(5, state.dailyXp);
  }
}
