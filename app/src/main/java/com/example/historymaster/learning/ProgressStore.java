package com.example.historymaster.learning;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.historymaster.models.QuizQuestion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.*;

public final class ProgressStore {
  private static final Gson GSON = new Gson();

  private static SharedPreferences prefs(Context c) {
    return c.getSharedPreferences("HistoryMasterPrefs", Context.MODE_PRIVATE);
  }

  public static synchronized LearningState load(Context c) {
    try {
      LearningState state =
          GSON.fromJson(prefs(c).getString("jipains_state_v1", ""), LearningState.class);
      if (state != null) return state;
    } catch (Exception ignored) {
    }
    return new LearningState();
  }

  private static void save(Context c, LearningState state) {
    prefs(c).edit().putString("jipains_state_v1", GSON.toJson(state)).apply();
  }

  public static synchronized int complete(
      Context c, String session, String lesson, String region, int score, int total) {
    LearningState state = load(c);
    int earned = state.complete(session, lesson, region, score, total, DayKey.today());
    save(c, state);
    return earned;
  }

  public static synchronized int read(Context c, String key) {
    LearningState state = load(c);
    int earned = state.read(key, DayKey.today());
    save(c, state);
    return earned;
  }

  public static int goal(Context c) {
    return prefs(c).getInt("daily_goal", 30);
  }

  public static void goal(Context c, int value) {
    prefs(c).edit().putInt("daily_goal", value).apply();
  }

  public static boolean setting(Context c, String key, boolean fallback) {
    return prefs(c).getBoolean(key, fallback);
  }

  public static void setSetting(Context c, String key, boolean value) {
    prefs(c).edit().putBoolean(key, value).apply();
  }

  public static synchronized List<QuizQuestion> review(Context c) {
    try {
      List<QuizQuestion> list =
          GSON.fromJson(
              prefs(c).getString("review_v1", "[]"),
              new TypeToken<List<QuizQuestion>>() {}.getType());
      return list == null ? new ArrayList<>() : list;
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  public static synchronized void recordAnswer(Context c, QuizQuestion question, boolean correct) {
    List<QuizQuestion> list = review(c);
    list.removeIf(q -> q.getId().equals(question.getId()));
    if (!correct) list.add(question);
    prefs(c).edit().putString("review_v1", GSON.toJson(list)).apply();
  }

  public static void saveSession(Context c, String key, String json) {
    prefs(c).edit().putString("session_" + key, json).apply();
  }

  public static String session(Context c, String key) {
    return prefs(c).getString("session_" + key, null);
  }

  public static void clearSession(Context c, String key) {
    prefs(c).edit().remove("session_" + key).apply();
  }
}
