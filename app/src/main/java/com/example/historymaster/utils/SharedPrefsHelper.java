package com.example.historymaster.utils;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.*;
public final class SharedPrefsHelper  {
    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("HistoryMasterPrefs",Context.MODE_PRIVATE);
    }
    public static void saveCurrentRegion(Context c,String r) {
        prefs(c).edit().putString("current_region",r).apply();
    }
    public static String getCurrentRegion(Context c,String d) {
        return prefs(c).getString("current_region",d);
    }
    public static void saveQuizScore(Context c,String r,int score) {
        prefs(c).edit().putInt("best_"+r,Math.max(score,getQuizScore(c,r))).apply();
    }
    public static int getQuizScore(Context c,String r) {
        return prefs(c).getInt("best_"+r,0);
    }
    public static boolean isRead(Context c,String r,String t) {
        return prefs(c).getStringSet("read_"+r,Collections.emptySet()).contains(t);
    }
    public static int readCount(Context c,String r) {
        return prefs(c).getStringSet("read_"+r,Collections.emptySet()).size();
    }
    public static void markRead(Context c,String r,String t) {
        Set<String> set=new HashSet<>(prefs(c).getStringSet("read_"+r,Collections.emptySet()));
        set.add(t);
        prefs(c).edit().putStringSet("read_"+r,set).apply();
    }
}
