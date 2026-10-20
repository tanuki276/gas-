package com.example.historymaster.api;

import static org.junit.Assert.*;

import org.junit.Test;

public class WikiParserTest {
  private String response(String page) {
    return "{\"query\":{\"pages\":[" + page + "]}}";
  }

  @Test
  public void preservesCanonicalTitleRevisionAndAttribution() {
    WikiArticle article =
        WikiParser.parse(
            response(
                "{\"ns\":0,\"pageid\":10,\"lastrevid\":22,\"title\":\"元"
                    + " (王朝)\",\"extract\":\"説明\"}"),
            123L);
    assertEquals("元 (王朝)", article.title);
    assertEquals(22, article.revisionId);
    assertEquals(123, article.retrievedAt);
    assertEquals("https://ja.wikipedia.org/w/index.php?oldid=22", article.revisionUrl());
  }

  @Test
  public void rejectsMissingAmbiguousEmptyAndNonArticlePages() {
    for (String page :
        new String[] {
          "{\"missing\":true}",
          "{\"pageprops\":{\"disambiguation\":\"\"}}",
          "{\"ns\":1}",
          "{\"ns\":0,\"title\":\"A\",\"extract\":\" \"}"
        }) {
      try {
        WikiParser.parse(response(page), 0);
        fail("Invalid page accepted");
      } catch (IllegalArgumentException expected) {
      }
    }
  }
}
