package com.historymaster.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {
    
    private static final String PREFS_NAME = "HistoryMasterPrefs";
    private static final String KEY_CURRENT_REGION = "current_region";
    private static final String KEY_QUIZ_SCORES = "quiz_scores";
    private static final String KEY_LEARNING_PROGRESS = "learning_progress";
    
    public static void saveCurrentRegion(Context context, String region) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CURRENT_REGION, region).apply();
    }
    
    public static String getCurrentRegion(Context context, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENT_REGION, defaultValue);
    }
    
    public static void saveQuizScore(Context context, String region, int score) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String scores = prefs.getString(KEY_QUIZ_SCORES, "{}");
        // JSON形式で保存
        // 実際にはGsonなどでオブジェクトを保存
    }
    
    public static int getQuizScore(Context context, String region) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // スコアを取得
        return 0;
    }
}