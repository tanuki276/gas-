package com.example.historymaster.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPrefsHelper {

    private static final String PREFS_NAME = "HistoryMasterPrefs";
    private static final String KEY_CURRENT_REGION = "current_region";
    private static final String KEY_QUIZ_SCORE_PREFIX = "quiz_score_";
    private static final String KEY_TEST_SCORE_PREFIX = "test_score_";
    private static final String KEY_LEARNING_PROGRESS_PREFIX = "learning_progress_";

    private SharedPrefsHelper() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveCurrentRegion(Context context, String region) {
        if (context == null || region == null) return;
        prefs(context).edit().putString(KEY_CURRENT_REGION, region).apply();
    }

    public static String getCurrentRegion(Context context, String defaultValue) {
        if (context == null) return defaultValue;
        return prefs(context).getString(KEY_CURRENT_REGION, defaultValue);
    }

    public static void saveQuizScore(Context context, String region, int score) {
        if (context == null || region == null) return;
        String key = KEY_QUIZ_SCORE_PREFIX + region;
        int best = Math.max(score, prefs(context).getInt(key, 0));
        prefs(context).edit().putInt(key, best).apply();
    }

    public static int getQuizScore(Context context, String region) {
        if (context == null || region == null) return 0;
        return prefs(context).getInt(KEY_QUIZ_SCORE_PREFIX + region, 0);
    }

    public static void saveTestScore(Context context, String region, int score) {
        if (context == null || region == null) return;
        String key = KEY_TEST_SCORE_PREFIX + region;
        int best = Math.max(score, prefs(context).getInt(key, 0));
        prefs(context).edit().putInt(key, best).apply();
    }

    public static int getTestScore(Context context, String region) {
        if (context == null || region == null) return 0;
        return prefs(context).getInt(KEY_TEST_SCORE_PREFIX + region, 0);
    }

    public static void saveLearningProgress(Context context, String region, int progress) {
        if (context == null || region == null) return;
        int clamped = Math.max(0, Math.min(100, progress));
        prefs(context).edit().putInt(KEY_LEARNING_PROGRESS_PREFIX + region, clamped).apply();
    }

    public static int getLearningProgress(Context context, String region) {
        if (context == null || region == null) return 0;
        return prefs(context).getInt(KEY_LEARNING_PROGRESS_PREFIX + region, 0);
    }
}
