package com.example.historymaster.ui;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.widget.*;
import com.example.historymaster.api.WikipediaApi;
import com.example.historymaster.utils.SharedPrefsHelper;
import java.util.concurrent.Future;
public class ArticleActivity extends Screen  {
    private Future<?> request;
    private String title;
    private TextView content,status;
    private Button read,retry;
    private SharedPreferences cache;
    private int generation;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        title=getIntent().getStringExtra("title");
        if(title==null||title.trim().isEmpty()) {
            finish();
            return;
        }
        cache=getSharedPreferences("wikipedia_cache",MODE_PRIVATE);
        page(title,"Wikipediaで知る · 日本語版の記事冒頭を表示");
        status=text("読み込み中…",14,false);
        content=text("",18,false);
        content.setTextIsSelectable(true);
        read=button(SharedPrefsHelper.isRead(this,region,title)?"✓ 読了済み":"読み終えた",()-> {
            SharedPrefsHelper.markRead(this,region,title);read.setText("✓ 読了済み");
        }
        ,true);
        read.setEnabled(false);
        retry=button("再取得",()->load(),false);
        button("Wikipediaで全文・編集履歴を見る ↗",()->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(WikipediaApi.articleUrl(title)))),false);
        text("出典：日本語版Wikipedia / 執筆者：各記事の投稿者。本文は原文の抜粋です。CC BY-SA（詳細・追加条件は元記事を参照）。教材の項目名と記事名が一致しない場合は、Wikipediaで検索してください。",12,false);
        button("Wikipediaで記事名を検索 ↗",()->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://ja.wikipedia.org/w/index.php?search="+Uri.encode(title)))),false);
        load();
    }
    private void load() {
        final int token=++generation;
        if(request!=null)request.cancel(true);
        String saved=cache.getString(title,null);
        if(saved!=null) {
            content.setText(saved);
            read.setEnabled(true);
        }
        status.setText(saved==null?"読み込み中…":"保存した解説を表示 · 更新を確認中…");
        retry.setEnabled(false);
        request=WikipediaApi.getSummary(title,new WikipediaApi.ApiCallback() {
            private boolean valid() {
                return token==generation&&!isFinishing()&&!isDestroyed();
            }
            public void onSuccess(String result) {
                if(!valid())return;cache.edit().putString(title,result).apply();content.setText(result);status.setText("Wikipediaより取得 · 原文の抜粋");read.setEnabled(true);retry.setEnabled(true);
            }
            public void onError(String message) {
                if(!valid())return;status.setText((saved==null?"":"保存した解説を表示中。\n")+message);retry.setEnabled(true);
            }
        }
        );
    }
    @Override protected void onDestroy() {
        generation++;
        if(request!=null)request.cancel(true);
        super.onDestroy();
    }
}
