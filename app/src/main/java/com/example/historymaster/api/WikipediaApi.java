package com.example.historymaster.api;
import android.os.Handler;
import android.os.Looper;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
public final class WikipediaApi  {
    private static final ExecutorService EXECUTOR=Executors.newFixedThreadPool(2);
    public interface ApiCallback  {
        void onSuccess(String result);
        void onError(String error);
    }
    public static String articleUrl(String title) {
        try {
            return "https://ja.wikipedia.org/wiki/"+URLEncoder.encode(title,"UTF-8").replace("+","%20");
        }
        catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    public static Future<?> getSummary(String title,ApiCallback callback) {
        Handler main=new Handler(Looper.getMainLooper());
        return EXECUTOR.submit(()-> {
            HttpURLConnection connection=null; try  {
                String url="https://ja.wikipedia.org/w/api.php?action=query&format=json&formatversion=2&prop=extracts%7Cpageprops&exintro=1&explaintext=1&exchars=1200&redirects=1&titles="+URLEncoder.encode(title,"UTF-8"); connection=(HttpURLConnection)new URL(url).openConnection();connection.setConnectTimeout(10000);connection.setReadTimeout(15000);connection.setRequestProperty("User-Agent","HistoryMaster/2.0 (https://github.com/tanuki276/gas-)"); if(connection.getResponseCode()!=200)throw new IOException();StringBuilder raw=new StringBuilder(); try(BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"))) {
                    String line;while((line=reader.readLine())!=null)raw.append(line);
                }
                JSONObject page=new JSONObject(raw.toString()).getJSONObject("query").getJSONArray("pages").getJSONObject(0); if(page.has("missing")||(page.optJSONObject("pageprops")!=null&&page.getJSONObject("pageprops").has("disambiguation")))throw new IOException(); String extract=page.optString("extract").trim();if(extract.isEmpty())throw new IOException(); if(!Thread.currentThread().isInterrupted())main.post(()->callback.onSuccess(extract));
            }
            catch(Exception e) {
                if(!Thread.currentThread().isInterrupted())main.post(()->callback.onError("解説を取得できませんでした。通信状態、または記事名をご確認ください。"));
            }
            finally {
                if(connection!=null)connection.disconnect();
            }
        }
        );
    }
}
