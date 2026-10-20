package com.example.historymaster.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.historymaster.learning.ProgressStore;
import java.util.*;

public final class SharedPrefsHelper {
  private static SharedPreferences prefs(Context c) {
    return c.getSharedPreferences("HistoryMasterPrefs", Context.MODE_PRIVATE);
  }

  public static void saveCurrentRegion(Context c, String r) {
    prefs(c).edit().putString("current_region", r).apply();
  }

  public static String getCurrentRegion(Context c, String fallback) {
    return prefs(c).getString("current_region", fallback);
  }

  public static void saveQuizScore(Context c, String r, int score) {
    prefs(c).edit().putInt("best_" + r, Math.max(score, getQuizScore(c, r))).apply();
  }

  public static int getQuizScore(Context c, String r) {
    return prefs(c).getInt("best_" + r, 0);
  }

  public static boolean isRead(Context c, String r, String title) {
    return prefs(c).getStringSet("read_" + r, Collections.emptySet()).contains(title);
  }

  public static int readCount(Context c, String r) {
    return prefs(c).getStringSet("read_" + r, Collections.emptySet()).size();
  }

  public static synchronized int markRead(Context c, String r, String title) {
    Set<String> read = new HashSet<>(prefs(c).getStringSet("read_" + r, Collections.emptySet()));
    if (!read.add(title)) return 0;
    int earned = ProgressStore.read(c, r + "\u001f" + title);
    prefs(c).edit().putStringSet("read_" + r, read).apply();
    return earned;
  }

  public static Set<String> favorites(Context c) {
    return new HashSet<>(prefs(c).getStringSet("favorites", Collections.emptySet()));
  }

  public static boolean favorite(Context c, String r, String title) {
    return favorites(c).contains(r + "\u001f" + title);
  }

  public static void toggleFavorite(Context c, String r, String title) {
    Set<String> favorites = favorites(c);
    String key = r + "\u001f" + title;
    if (!favorites.remove(key)) favorites.add(key);
    prefs(c).edit().putStringSet("favorites", favorites).apply();
  }
}
