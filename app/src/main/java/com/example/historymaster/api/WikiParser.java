package com.example.historymaster.api;

import com.google.gson.*;

/** Parsing and page rejection are separate from networking and Android lifecycles. */
public final class WikiParser {
  public static WikiArticle parse(String json, long fetchedAt) {
    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
    if (root.has("error")) throw new IllegalArgumentException("API_ERROR");
    JsonObject page =
        root.getAsJsonObject("query").getAsJsonArray("pages").get(0).getAsJsonObject();
    if (page.has("missing") || page.has("invalid")) throw new IllegalArgumentException("MISSING");
    if (page.has("pageprops") && page.getAsJsonObject("pageprops").has("disambiguation"))
      throw new IllegalArgumentException("AMBIGUOUS");
    if (page.has("ns") && page.get("ns").getAsInt() != 0)
      throw new IllegalArgumentException("MISSING");
    WikiArticle article = new WikiArticle();
    article.title = page.get("title").getAsString();
    article.extract = page.has("extract") ? page.get("extract").getAsString().trim() : "";
    if (article.extract.isEmpty()) throw new IllegalArgumentException("EMPTY");
    article.pageId = page.get("pageid").getAsLong();
    article.revisionId = page.get("lastrevid").getAsLong();
    if (article.pageId <= 0 || article.revisionId <= 0)
      throw new IllegalArgumentException("MISSING_METADATA");
    article.retrievedAt = fetchedAt;
    return article;
  }
}
