package com.historymaster.api;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WikipediaApi {
    
    public interface ApiCallback {
        void onSuccess(String result);
        void onError(String error);
    }
    
    public static void getSummary(String title, ApiCallback callback) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    String encodedTitle = java.net.URLEncoder.encode(params[0], "UTF-8");
                    String url = "https://ja.wikipedia.org/api/rest_v1/page/summary/" + encodedTitle;
                    
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "HistoryMaster/1.0");
                    
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject json = new JSONObject(response.toString());
                    return json.optString("extract", "情報が見つかりませんでした");
                    
                } catch (Exception e) {
                    return null;
                }
            }
            
            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError("情報を取得できませんでした");
                }
            }
        }.execute(title);
    }
}