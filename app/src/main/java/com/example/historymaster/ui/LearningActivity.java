package com.example.historymaster.ui;
import android.os.Bundle;
import com.example.historymaster.models.Entity;
import com.example.historymaster.utils.SharedPrefsHelper;
public class LearningActivity extends Screen  {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
    }
    @Override protected void onResume() {
        super.onResume();
        page("自分のペースで学ぶ","解説を読み終えたら「読了」をタップ。進捗は端末に保存されます。");
        if(!ready())return;
        java.util.List<Entity> all=entities();
        int count=0;
        for(Entity e:all)if(SharedPrefsHelper.isRead(this,region,e.getName()))count++;
        text(count+" / "+all.size()+" 項目を読了",24,true);
        android.widget.ProgressBar bar=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        bar.setMax(all.size());
        bar.setProgress(count);
        body.addView(bar);
        for(Entity e:all)button((SharedPrefsHelper.isRead(this,region,e.getName())?"✓ 読了  ":"読む  ")+e.getName(),()->article(e.getName()),false);
    }
}
