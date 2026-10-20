package com.example.historymaster.ui;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import com.example.historymaster.api.*;
import com.example.historymaster.design.*;
import com.example.historymaster.utils.SharedPrefsHelper;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.*;

public class ArticleActivity extends Screen {
  private WikipediaApi.Request request;
  private String title, lookup;
  private TextView content, status, credit;
  private Button read, retry, favorite, original, revision, history;
  private SharedPreferences cache;
  private WikiArticle article;
  private int generation;
  private final Gson gson = new Gson();

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    title = getIntent().getStringExtra("title");
    if (title == null || title.trim().isEmpty()) {
      finish();
      return;
    }
    lookup = WikiTitles.resolve(this, region, title);
    cache = getSharedPreferences("wiki_articles_v2", MODE_PRIVATE);
    page(title, "世界の図鑑 / 日本語版Wikipedia");
    favorite =
        button(
            "",
            () -> {
              SharedPrefsHelper.toggleFavorite(this, region, title);
              favoriteLabel();
            },
            false);
    favoriteLabel();
    status = text("解説を読み込み中…", 14, false);
    LinearLayout sheet = card(Palette.WHITE);
    content = label(sheet, "", 18, Palette.INK, false);
    content.setTextIsSelectable(true);
    read =
        button(
            SharedPrefsHelper.isRead(this, region, title) ? "✓ 読了済み" : "読み終えた  +5 XP",
            () -> {
              int earned = SharedPrefsHelper.markRead(this, region, title);
              read.setText("✓ 読了済み");
              read.setEnabled(false);
              if (earned > 0) {
                Toast.makeText(this, "新しい発見！ +" + earned + " XP", Toast.LENGTH_SHORT).show();
                celebrate();
              }
            },
            true);
    read.setEnabled(false);
    retry = button("解説を再取得", this::load, false);
    text("出典と記事情報", 20, true);
    credit = text("", 12, false);
    original =
        button(
            "Wikipediaの全文を読む ↗",
            () -> browse(article == null ? WikipediaApi.articleUrl(lookup) : article.url()),
            false);
    revision =
        button(
            "取得時の記事版を見る ↗",
            () -> {
              if (article != null) browse(article.revisionUrl());
            },
            false);
    revision.setEnabled(false);
    history =
        button(
            "投稿者・編集履歴を見る ↗",
            () ->
                browse(
                    "https://ja.wikipedia.org/w/index.php?title="
                        + Uri.encode(article == null ? lookup : article.title)
                        + "&action=history"),
            false);
    button(
        "CC BY-SA 4.0 の利用条件 ↗",
        () -> browse("https://creativecommons.org/licenses/by-sa/4.0/deed.ja"),
        false);
    button(
        "Wikipediaで記事名を検索 ↗",
        () -> browse("https://ja.wikipedia.org/w/index.php?search=" + Uri.encode(title)),
        false);
    text(
        "本文は記事冒頭の抜粋です。翻訳・AIによる書き換えはしていません。原記事には追加条件がある場合があります。複合的な教材名に対応する記事がない場合は、検索をお使いください。",
        12,
        false);
    load();
  }

  private void favoriteLabel() {
    favorite.setText(
        SharedPrefsHelper.favorite(this, region, title) ? "★ 保存済み · タップで解除" : "☆ あとで読むために保存");
  }

  private void show(WikiArticle result, boolean saved) {
    article = result;
    content.setText(result.extract);
    String when =
        new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN).format(new Date(result.retrievedAt));
    status.setText(saved ? "保存した解説を表示しています" : "Wikipediaから取得しました");
    credit.setText(
        "「"
            + result.title
            + "」\n出典：日本語版Wikipedia / 著者：記事の投稿者\n取得："
            + when
            + " · 記事版ID："
            + result.revisionId
            + "\nライセンス："
            + result.licenseName);
    revision.setEnabled(true);
    read.setEnabled(!SharedPrefsHelper.isRead(this, region, title));
  }

  private void load() {
    final int token = ++generation;
    if (request != null) request.cancel();
    try {
      String saved = cache.getString(lookup, null);
      if (saved != null) {
        WikiArticle cached = gson.fromJson(saved, WikiArticle.class);
        if (cached != null && cached.pageId > 0 && cached.extract != null) show(cached, true);
      }
    } catch (Exception ignored) {
    }
    status.setText(article == null ? "解説を読み込み中…" : "保存した解説を表示 · 更新を確認中…");
    retry.setEnabled(false);
    request =
        WikipediaApi.getSummary(
            lookup,
            new WikipediaApi.ApiCallback() {
              private boolean valid() {
                return token == generation && !isFinishing() && !isDestroyed();
              }

              @Override
              public void onSuccess(WikiArticle result) {
                if (!valid()) return;
                cache.edit().putString(lookup, gson.toJson(result)).apply();
                show(result, false);
                retry.setEnabled(true);
              }

              @Override
              public void onError(String error) {
                if (!valid()) return;
                status.setText((article == null ? "" : "保存した解説を表示中。\n") + error);
                retry.setEnabled(true);
              }
            });
  }

  @Override
  protected void onDestroy() {
    generation++;
    if (request != null) request.cancel();
    super.onDestroy();
  }
}
