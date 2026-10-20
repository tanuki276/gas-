package com.example.historymaster.learning;

import com.example.historymaster.models.QuizQuestion;
import java.util.*;

public final class QuizSession {
  public String id = UUID.randomUUID().toString();
  public List<QuizQuestion> questions = new ArrayList<>();
  public int index, score, selected = -1, earned;
  public boolean answered, rewardApplied;

  public boolean answer(int option) {
    if (finished() || answered || option < 0 || option >= questions.get(index).getOptions().size())
      return false;
    selected = option;
    answered = true;
    if (questions.get(index).checkAnswer(option)) score++;
    return true;
  }

  public boolean next() {
    if (!answered || finished()) return false;
    index++;
    selected = -1;
    answered = false;
    return true;
  }

  public boolean finished() {
    return index >= questions.size();
  }
}
