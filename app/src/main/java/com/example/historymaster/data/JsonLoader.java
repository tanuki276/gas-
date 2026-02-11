package com.example.historymaster.data;

import android.content.Context;
import com.google.gson.Gson;
import com.example.historymaster.models.HistoryData;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonLoader {
    
    private static final Gson gson = new Gson();
    
    public static HistoryData loadHistoryData(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8"));
            
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            
            reader.close();
            inputStream.close();
            
            return gson.fromJson(stringBuilder.toString(), HistoryData.class);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getRegionFileName(String region) {
        return region + ".json";
    }
}