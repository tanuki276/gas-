package com.example.historymaster.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.example.historymaster.models.ReviewItem;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SharedPrefsHelper {

    private static final String PREFS_NAME = "JipainsPrefs";
    private static final String KEY_CURRENT_REGION = "current_region";
    private static final String KEY_QUIZ_SCORE_PREFIX = "quiz_score_";
    private static final String KEY_TEST_SCORE_PREFIX = "test_score_";
    private static final String KEY_LEARNING_PROGRESS_PREFIX = "learning_progress_";
    private static final String KEY_XP = "xp";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_LAST_STUDY_DAY = "last_study_day";
    private static final String KEY_REVIEW_ITEMS = "review_items";

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

    public static int addXp(Context context, int amount) {
        if (context == null || amount <= 0) return getXp(context);
        int next = getXp(context) + amount;
        prefs(context).edit().putInt(KEY_XP, next).apply();
        recordStudyDay(context);
        return next;
    }

    public static int getXp(Context context) {
        if (context == null) return 0;
        return prefs(context).getInt(KEY_XP, 0);
    }

    public static int getLevel(Context context) {
        return 1 + getXp(context) / 100;
    }

    public static int getStreak(Context context) {
        if (context == null) return 0;
        refreshStreak(context);
        return prefs(context).getInt(KEY_STREAK, 0);
    }

    private static void recordStudyDay(Context context) {
        SharedPreferences p = prefs(context);
        String today = dayKey(0);
        String yesterday = dayKey(-1);
        String last = p.getString(KEY_LAST_STUDY_DAY, "");
        if (today.equals(last)) return;

        int streak = p.getInt(KEY_STREAK, 0);
        streak = yesterday.equals(last) ? streak + 1 : 1;
        p.edit().putInt(KEY_STREAK, streak).putString(KEY_LAST_STUDY_DAY, today).apply();
    }

    private static void refreshStreak(Context context) {
        SharedPreferences p = prefs(context);
        String last = p.getString(KEY_LAST_STUDY_DAY, "");
        if (!last.isEmpty() && !last.equals(dayKey(0)) && !last.equals(dayKey(-1))) {
            p.edit().putInt(KEY_STREAK, 0).apply();
        }
    }

    private static String dayKey(int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendar.getTime());
    }

    public static void addReviewItem(Context context, String prompt, String answer) {
        if (context == null || prompt == null || answer == null) return;
        String cleanPrompt = prompt.trim();
        String cleanAnswer = answer.trim();
        if (cleanPrompt.isEmpty() || cleanAnswer.isEmpty()) return;

        Set<String> items = new HashSet<>(prefs(context).getStringSet(KEY_REVIEW_ITEMS, new HashSet<>()));
        String prefix = encode(cleanPrompt) + ":" + encode(cleanAnswer) + ":";
        items.removeIf(value -> value.startsWith(prefix));
        items.add(prefix + System.currentTimeMillis() + ":0");
        prefs(context).edit().putStringSet(KEY_REVIEW_ITEMS, items).apply();
    }

    public static List<ReviewItem> getDueReviewItems(Context context) {
        List<ReviewItem> result = new ArrayList<>();
        if (context == null) return result;
        long now = System.currentTimeMillis();
        for (String raw : prefs(context).getStringSet(KEY_REVIEW_ITEMS, new HashSet<>())) {
            ReviewItem item = decodeReview(raw);
            if (item != null && item.getDueAt() <= now) result.add(item);
        }
        return result;
    }

    public static int getDueReviewCount(Context context) {
        return getDueReviewItems(context).size();
    }

    public static void markReviewCorrect(Context context, ReviewItem item) {
        if (context == null || item == null) return;
        int nextInterval = item.getIntervalDays() <= 0 ? 1 : Math.min(30, item.getIntervalDays() * 2);
        long due = System.currentTimeMillis() + nextInterval * 24L * 60L * 60L * 1000L;
        replaceReview(context, item, due, nextInterval);
        addXp(context, 5);
    }

    public static void markReviewWrong(Context context, ReviewItem item) {
        if (context == null || item == null) return;
        long due = System.currentTimeMillis() + 10L * 60L * 1000L;
        replaceReview(context, item, due, 0);
    }

    private static void replaceReview(Context context, ReviewItem item, long due, int intervalDays) {
        Set<String> items = new HashSet<>(prefs(context).getStringSet(KEY_REVIEW_ITEMS, new HashSet<>()));
        String prefix = encode(item.getPrompt()) + ":" + encode(item.getAnswer()) + ":";
        items.removeIf(value -> value.startsWith(prefix));
        items.add(prefix + due + ":" + intervalDays);
        prefs(context).edit().putStringSet(KEY_REVIEW_ITEMS, items).apply();
    }

    private static ReviewItem decodeReview(String raw) {
        try {
            String[] parts = raw.split(":", 4);
            if (parts.length != 4) return null;
            return new ReviewItem(
                decode(parts[0]), decode(parts[1]), Long.parseLong(parts[2]), Integer.parseInt(parts[3]));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String encode(String value) {
        return Base64.encodeToString(value.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP | Base64.URL_SAFE);
    }

    private static String decode(String value) {
        return new String(Base64.decode(value, Base64.NO_WRAP | Base64.URL_SAFE), StandardCharsets.UTF_8);
    }
}
