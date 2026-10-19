package com.example.historymaster.data;

import android.content.Context;
import android.util.Log;

import com.example.historymaster.models.HistoryData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class JsonLoader {

    private static final String TAG = "JsonLoader";
    private static final Gson GSON = new Gson();
    private static final Set<String> SUPPORTED_REGIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "china", "japan", "korea", "mongol", "vietnam", "cambodia", "laos"
        ))
    );

    private JsonLoader() {
    }

    public static HistoryData loadHistoryData(Context context, String fileName) {
        if (context == null || fileName == null || fileName.trim().isEmpty()) {
            return null;
        }

        try (InputStream inputStream = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            HistoryData data = GSON.fromJson(reader, HistoryData.class);
            if (data == null || data.getPeriods() == null) {
                Log.e(TAG, "History data is empty or missing periods: " + fileName);
                return null;
            }
            return data;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Invalid history JSON: " + fileName, e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load history data: " + fileName, e);
        }
        return null;
    }

    public static String getRegionFileName(String region) {
        String safeRegion = normalizeRegion(region);
        return safeRegion + ".json";
    }

    public static String normalizeRegion(String region) {
        if (region == null) {
            return "china";
        }
        String normalized = region.trim().toLowerCase(java.util.Locale.ROOT);
        return SUPPORTED_REGIONS.contains(normalized) ? normalized : "china";
    }

    public static boolean isSupportedRegion(String region) {
        if (region == null) {
            return false;
        }
        return SUPPORTED_REGIONS.contains(region.trim().toLowerCase(java.util.Locale.ROOT));
    }
}
