package com.example.historymaster;
import android.os.Bundle;
import com.example.historymaster.ui.*;
import com.example.historymaster.data.*;
import com.example.historymaster.utils.SharedPrefsHelper;
public class MainActivity extends Screen  {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        region=Regions.safe(SharedPrefsHelper.getCurrentRegion(this,"china"));
        render();
    }
    @Override protected void onResume() {
        super.onResume();
        render();
    }
    private void render() {
        int i=Regions.index(region);
        page("歴史を、旅しよう。","HISTORY MASTER  /  アジアの7地域を探究");
        text("今日の一歩",20,true);
        text("解説をひとつ読む。それだけでも、新しい発見につながります。",15,false);
        text("探索中 · "+Regions.NAMES[i],22,true);
        text("既読 "+SharedPrefsHelper.readCount(this,region)+"項目  ·  クイズ最高 "+SharedPrefsHelper.getQuizScore(this,region)+"点",14,false);
        button("はじめに読む → "+Regions.NAMES[i]+"の歴史",()->article(Regions.ARTICLES[Regions.index(region)]),true);
        button("時代・国・王朝を探す",()->open(ExplanationActivity.class),false);
        button("学習を続ける",()->open(LearningActivity.class),false);
        button("5問のクイズに挑戦",()->open(QuizActivity.class),true);
        button("理解度テスト · 10問",()->open(TestActivity.class),false);
        button("キーワードで検索",()->open(SearchActivity.class),false);
        text("次はどこを旅する？",22,true);
        for(int n=0;n<Regions.IDS.length;n++) {
            final int k=n;
            button((i==n?"✓  ":"")+Regions.NAMES[n],()-> {
                region=Regions.IDS[k];SharedPrefsHelper.saveCurrentRegion(this,region);render();
            }
            ,i==n);
        }
    }
}
