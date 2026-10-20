package com.example.historymaster.api;

import android.os.Handler;
import android.os.Looper;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public final class WikipediaApi {
  private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

  public interface ApiCallback {
    void onSuccess(WikiArticle article);

    void onError(String message);
  }

  public static final class Request {
    private volatile boolean canceled;
    private volatile HttpURLConnection connection;
    private Future<?> future;

    public void cancel() {
      canceled = true;
      if (future != null) future.cancel(true);
      if (connection != null) connection.disconnect();
    }
  }

  public static String articleUrl(String title) {
    return "https://ja.wikipedia.org/wiki/" + encoded(title);
  }

  private static String encoded(String text) {
    try {
      return URLEncoder.encode(text, "UTF-8").replace("+", "%20");
    } catch (UnsupportedEncodingException impossible) {
      throw new IllegalStateException(impossible);
    }
  }

  public static Request getSummary(String title, ApiCallback callback) {
    Request request = new Request();
    Handler main = new Handler(Looper.getMainLooper());
    request.future =
        EXECUTOR.submit(
            () -> {
              try {
                String url =
                    "https://ja.wikipedia.org/w/api.php?action=query&format=json&formatversion=2&prop=extracts%7Cpageprops%7Cinfo&exintro=1&explaintext=1&exchars=1200&redirects=1&titles="
                        + encoded(title);
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                request.connection = connection;
                if (request.canceled) return;
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty(
                    "User-Agent", "Jipains/3.0 (https://github.com/tanuki276/gas-)");
                if (connection.getResponseCode() != 200)
                  throw new IOException("HTTP " + connection.getResponseCode());
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                  String line;
                  while ((line = reader.readLine()) != null) {
                    if (request.canceled) return;
                    response.append(line);
                    if (response.length() > 500000) throw new IOException("Too large");
                  }
                }
                WikiArticle article =
                    WikiParser.parse(response.toString(), System.currentTimeMillis());
                main.post(
                    () -> {
                      if (!request.canceled) callback.onSuccess(article);
                    });
              } catch (Exception e) {
                String reason = e.getMessage();
                String message =
                    "AMBIGUOUS".equals(reason)
                        ? "同名の記事が複数あります。Wikipediaで目的の記事を選んでください。"
                        : "MISSING".equals(reason) || "EMPTY".equals(reason)
                            ? "この項目に対応する解説が見つかりません。下の検索から記事を探せます。"
                            : "解説を取得できませんでした。通信状態を確認して、再取得をお試しください。";
                main.post(
                    () -> {
                      if (!request.canceled) callback.onError(message);
                    });
              } finally {
                if (request.connection != null) request.connection.disconnect();
              }
            });
    return request;
  }
}
