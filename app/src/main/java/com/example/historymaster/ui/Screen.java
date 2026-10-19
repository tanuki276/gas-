package com.example.historymaster.ui;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.historymaster.data.*;
import com.example.historymaster.models.*;
import java.util.*;
public abstract class Screen extends AppCompatActivity  {
    protected LinearLayout body;
    protected String region;
    protected HistoryData data;
    protected final int ink=Color.rgb(25,43,64), teal=Color.rgb(0,104,105);
    protected int dp(int n) {
        return Math.round(n*getResources().getDisplayMetrics().density);
    }
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        region=Regions.safe(getIntent().getStringExtra("region"));
        data=JsonLoader.loadHistoryData(this,region+".json");
    }
    protected void page(String title,String subtitle) {
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(244,246,249));
        body=new LinearLayout(this);
        body.setOrientation(1);
        body.setPadding(dp(22),dp(18),dp(22),dp(32));
        scroll.addView(body);
        setContentView(scroll);
        if(!(this instanceof com.example.historymaster.MainActivity))button("‹ ホームに戻る",()->finish(),false);
        text(title,30,true);
        if(subtitle!=null)text(subtitle,15,false);
    }
    protected TextView text(String s,int size,boolean bold) {
        TextView t=new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(ink);
        t.setLineSpacing(dp(4),1);
        t.setPadding(0,dp(10),0,dp(10));
        if(bold)t.setTypeface(null,Typeface.BOLD);
        body.addView(t);
        if(bold && android.os.Build.VERSION.SDK_INT>=28)t.setAccessibilityHeading(true);
        return t;
    }
    protected Button button(String s,Runnable action,boolean primary) {
        com.google.android.material.button.MaterialButton b=new com.google.android.material.button.MaterialButton(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setMinHeight(dp(56));
        b.setCornerRadius(dp(16));
        b.setTextColor(primary?Color.WHITE:ink);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary?teal:Color.WHITE));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,dp(5),0,dp(5));
        body.addView(b,lp);
        b.setOnClickListener(v->action.run());
        return b;
    }
    protected void open(Class<?> cls) {
        Intent i=new Intent(this,cls);
        i.putExtra("region",region);
        startActivity(i);
    }
    protected List<Entity> entities() {
        List<Entity> r=new ArrayList<>();
        if(data!=null&&data.getPeriods()!=null)for(Period p:data.getPeriods())collect(p.getMiddleLevelEntities(),r);
        return r;
    }
    private void collect(List<Entity> es,List<Entity> r) {
        if(es==null)return;
        for(Entity e:es) {
            r.add(e);
            collect(e.getSubEntities(),r);
        }
    }
    protected void article(String title) {
        Intent i=new Intent(this,ArticleActivity.class);
        i.putExtra("region",region);
        i.putExtra("title",title);
        startActivity(i);
    }
    protected boolean ready() {
        if(data!=null&&data.getPeriods()!=null)return true;
        text("教材を読み込めませんでした。ホームからもう一度お試しください。",16,false);
        return false;
    }
}
