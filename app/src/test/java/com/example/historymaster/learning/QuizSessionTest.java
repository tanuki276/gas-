package com.example.historymaster.learning;

import static org.junit.Assert.*;

import com.example.historymaster.models.QuizQuestion;
import com.google.gson.Gson;
import java.util.*;
import org.junit.Test;

public class QuizSessionTest {
  @Test
  public void cannotSkipOrScoreTwiceAndRestoresAnsweredState() {
    QuizSession session = new QuizSession();
    session.questions.add(new QuizQuestion("Q", Arrays.asList("A", "B"), 1, "course"));
    assertFalse(session.next());
    assertFalse(session.answer(-1));
    assertFalse(session.answer(2));
    assertTrue(session.answer(1));
    assertFalse(session.answer(1));
    assertEquals(1, session.score);
    QuizSession restored = new Gson().fromJson(new Gson().toJson(session), QuizSession.class);
    assertTrue(restored.answered);
    assertEquals(session.id, restored.id);
    assertEquals(1, restored.selected);
    assertFalse(restored.answer(1));
    assertTrue(restored.next());
    assertTrue(restored.finished());
    assertFalse(restored.next());
    assertFalse(restored.answer(0));
  }
}
