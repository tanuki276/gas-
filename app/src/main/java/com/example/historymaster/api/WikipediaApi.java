package com.example.historymaster.api;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WikipediaApi {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private WikipediaApi() {
    }

    public interface ApiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void getSummary(String title, ApiCallback callback) {
        if (callback == null) return;
        if (title == null || title.trim().isEmpty()) {
            callback.onError("検索語を入力してください");
            return;
        }

        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String encodedTitle = URLEncoder.encode(title.trim(), "UTF-8").replace("+", "%20");
                URL url = new URL("https://ja.wikipedia.org/api/rest_v1/page/summary/" + encodedTitle);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "HistoryMaster/1.0 (Android educational app)");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream();
                String body = readAll(stream);

                if (status < 200 || status >= 300) {
                    postError(callback, status == 404 ? "情報が見つかりませんでした" : "Wikipediaへの接続に失敗しました (" + status + ")");
                    return;
                }

                JSONObject json = new JSONObject(body);
                String extract = json.optString("extract", "").trim();
                if (extract.isEmpty()) {
                    postError(callback, "情報が見つかりませんでした");
                } else {
                    MAIN.post(() -> callback.onSuccess(extract));
                }
            } catch (Exception e) {
                postError(callback, "情報を取得できませんでした");
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            return response.toString();
        }
    }

    private static void postError(ApiCallback callback, String message) {
        MAIN.post(() -> callback.onError(message));
    }
}
