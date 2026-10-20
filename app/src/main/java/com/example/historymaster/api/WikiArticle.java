package com.example.historymaster.api;

public final class WikiArticle {
  public String title, extract;
  public long pageId, revisionId, retrievedAt;
  public String licenseName = "CC BY-SA 4.0";
  public String licenseUrl = "https://creativecommons.org/licenses/by-sa/4.0/deed.ja";

  public String url() {
    return "https://ja.wikipedia.org/?curid=" + pageId;
  }

  public String revisionUrl() {
    return "https://ja.wikipedia.org/w/index.php?oldid=" + revisionId;
  }
}
