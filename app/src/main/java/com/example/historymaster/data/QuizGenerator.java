package com.example.historymaster.data;
import com.example.historymaster.models.*;
import java.util.*;
public final class QuizGenerator  {
    public static List<QuizQuestion> generateQuiz(HistoryData data,int count) {
        List<Entity> all=new ArrayList<>();
        if(data==null||data.getPeriods()==null||count<=0)return new ArrayList<>();
        for(Period p:data.getPeriods())collect(p.getMiddleLevelEntities(),all);
        List<QuizQuestion> pool=new ArrayList<>();
        for(Entity e:all) {
            if(e.getTags()==null)continue;
            for(String tag:e.getTags()) {
                Set<String> ambiguous=new HashSet<>();
                for(Entity other:all)if(other.getTags()!=null&&other.getTags().contains(tag))ambiguous.add(other.getName());
                List<String> wrong=new ArrayList<>();
                for(Entity other:all)if(!ambiguous.contains(other.getName())&&!wrong.contains(other.getName()))wrong.add(other.getName());
                if(wrong.isEmpty())continue;
                Collections.shuffle(wrong);
                List<String> options=new ArrayList<>(wrong.subList(0,Math.min(3,wrong.size())));
                options.add(e.getName());
                Collections.shuffle(options);
                QuizQuestion q=new QuizQuestion("教材で「"+tag+"」に関連づけられているのは？",options,options.indexOf(e.getName()),"tag");
                q.setExplanation("教材の関連語："+e.getName()+" → "+join(e.getTags()));
                pool.add(q);
            }
        }
        Collections.shuffle(pool);
        return new ArrayList<>(pool.subList(0,Math.min(count,pool.size())));
    }
    private static String join(List<String> tags) {
        StringBuilder text=new StringBuilder();
        for(String tag:tags) {
            if(text.length()>0)text.append("・");
            text.append(tag);
        }
        return text.toString();
    }
    private static void collect(List<Entity> list,List<Entity> all) {
        if(list==null)return;
        for(Entity e:list) {
            all.add(e);
            collect(e.getSubEntities(),all);
        }
    }
}
