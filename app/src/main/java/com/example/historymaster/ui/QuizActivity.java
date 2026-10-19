package com.example.historymaster.ui;
import android.os.Bundle;
import android.widget.Button;
import com.example.historymaster.models.*;
import com.example.historymaster.data.QuizGenerator;
import com.example.historymaster.utils.SharedPrefsHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.*;
public class QuizActivity extends Screen  {
    private List<QuizQuestion> questions;
    private int index,score,selected=-1;
    private boolean answered;
    private final List<Button> options=new ArrayList<>();
    protected int questionCount() {
        return 5;
    }
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if(b!=null) {
            questions=new Gson().fromJson(b.getString("questions"),new TypeToken<List<QuizQuestion>>() {
            }
            .getType());
            index=b.getInt("index");
            score=b.getInt("score");
            answered=b.getBoolean("answered");
            selected=b.getInt("selected",-1);
        }
        else questions=QuizGenerator.generateQuiz(data,questionCount());
        render();
    }
    private void render() {
        page(questionCount()==5?"発見をクイズに。":"理解度テスト","時間制限なし · 教材の関連語から出題");
        if(questions==null||questions.isEmpty()) {
            text("この教材では出題できません。先に解説をお楽しみください。",16,false);
            return;
        }
        if(index>=questions.size()) {
            int percent=Math.round(score*100f/questions.size());
            text("おつかれさまでした！",26,true);
            text(score+" / "+questions.size()+" 問正解 · "+percent+"点",22,true);
            SharedPrefsHelper.saveQuizScore(this,region,percent);
            button("もう一度挑戦",()-> {
                questions=QuizGenerator.generateQuiz(data,questionCount());index=score=0;answered=false;selected=-1;render();
            }
            ,true);
            button("解説で振り返る",()->open(ExplanationActivity.class),false);
            return;
        }
        QuizQuestion q=questions.get(index);
        text((index+1)+" / "+questions.size()+" 問",16,false);
        text(q.getQuestion(),23,true);
        options.clear();
        for(int k=0;k<q.getOptions().size();k++) {
            final int n=k;
            Button option=button(q.getOptions().get(k),()->answer(n),false);
            option.setEnabled(!answered);
            options.add(option);
        }
        if(answered) {
            text((q.checkAnswer(selected)?"✓ 正解！":"次の発見につなげよう。正解："+q.getOptions().get(q.getCorrectIndex()))+"\n\n"+q.getExplanation(),17,true);
            button("正解の項目を読む",()->article(q.getOptions().get(q.getCorrectIndex())),false);
            button(index+1==questions.size()?"結果を見る":"次の問題へ",()-> {
                index++;answered=false;selected=-1;render();
            }
            ,true);
        }
    }
    private void answer(int n) {
        if(answered)return;
        selected=n;
        answered=true;
        if(questions.get(index).checkAnswer(n))score++;
        render();
    }
    @Override protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putString("questions",new Gson().toJson(questions));
        b.putInt("index",index);
        b.putInt("score",score);
        b.putBoolean("answered",answered);
        b.putInt("selected",selected);
    }
}
