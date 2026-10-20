package com.example.historymaster.learning;

import com.example.historymaster.models.QuizQuestion;
import java.util.*;

public class Lesson {
  public String id, region, title, subtitle, sourceTitle;
  public List<Fact> facts = new ArrayList<>();
  public boolean checkpoint;

  public static class Fact {
    public String question, answer, note;
    public List<String> distractors;
  }

  public List<QuizQuestion> questions() {
    List<QuizQuestion> result = new ArrayList<>();
    for (int i = 0; i < facts.size(); i++) {
      Fact fact = facts.get(i);
      List<String> choices = new ArrayList<>(fact.distractors);
      choices.add(fact.answer);
      Collections.shuffle(choices);
      QuizQuestion q =
          new QuizQuestion(fact.question, choices, choices.indexOf(fact.answer), "course");
      q.setExplanation(fact.note);
      q.setIdentity(id + ":" + i, region, sourceTitle);
      result.add(q);
    }
    return result;
  }
}
