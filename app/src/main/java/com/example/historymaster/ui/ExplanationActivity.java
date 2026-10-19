package com.example.historymaster.ui;
import android.os.Bundle;
import com.example.historymaster.data.Regions;
import com.example.historymaster.models.*;
public class ExplanationActivity extends Screen  {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        page(Regions.NAMES[Regions.index(region)]+"の歴史","時代から探す · 項目をタップするとWikipedia解説が開きます");
        button("地域の歴史を概観する",()->article(Regions.ARTICLES[Regions.index(region)]),true);
        if(!ready())return;
        text(data.getDescription(),15,false);
        for(Period p:data.getPeriods()) {
            text(p.getUpperLevel(),22,true);
            show(p.getMiddleLevelEntities(),0);
        }
    }
    private void show(java.util.List<Entity> list,int depth) {
        if(list==null)return;
        for(Entity e:list) {
            button((depth>0?"↳ ":"")+e.getName()+"\n"+(e.getTags()==null?"":android.text.TextUtils.join(" · ",e.getTags())),()->article(e.getName()),false);
            show(e.getSubEntities(),depth+1);
        }
    }
}
