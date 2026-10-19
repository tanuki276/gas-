package com.example.historymaster.ui;
import android.os.Bundle;
import android.widget.*;
import android.text.*;
import com.example.historymaster.models.Entity;
import java.util.Locale;
public class SearchActivity extends Screen  {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        page("歴史を検索","国・王朝・人物・キーワードから、選択中の地域の教材を探せます。");
        if(!ready())return;
        EditText input=new EditText(this);
        input.setHint("例：貿易、幕府、仏教");
        input.setTextColor(ink);
        input.setSingleLine(true);
        input.setMinHeight(dp(56));
        body.addView(input);
        LinearLayout results=new LinearLayout(this);
        results.setOrientation(1);
        body.addView(results);
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int c,int f) {
            }
            public void afterTextChanged(Editable e) {
            }
            public void onTextChanged(CharSequence s,int a,int before,int c) {
                render(s.toString(),results);
            }
        }
        );
        render("",results);
    }
    private void render(String q,LinearLayout results) {
        results.removeAllViews();
        LinearLayout root=body;
        body=results;
        int found=0;
        String query=q.trim().toLowerCase(Locale.ROOT);
        for(Entity e:entities()) {
            String tags=e.getTags()==null?"":TextUtils.join(" · ",e.getTags());
            if((e.getName()+tags).toLowerCase(Locale.ROOT).contains(query)) {
                found++;
                button(e.getName()+"\n"+tags,()->article(e.getName()),false);
            }
        }
        text(found==0?"該当する項目がありません。別の言葉で検索してください。":found+"件の項目",14,false);
        body=root;
    }
}
