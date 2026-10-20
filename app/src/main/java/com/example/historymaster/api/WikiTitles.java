package com.example.historymaster.api;

import android.content.Context;
import com.google.gson.*;
import java.io.*;

public final class WikiTitles {
  public static String resolve(Context context, String region, String title) {
    try (Reader reader =
        new InputStreamReader(context.getAssets().open("wiki_aliases.json"), "UTF-8")) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
      if (root.has(region) && root.getAsJsonObject(region).has(title))
        return root.getAsJsonObject(region).get(title).getAsString();
    } catch (Exception ignored) {
    }
    return title;
  }
}
