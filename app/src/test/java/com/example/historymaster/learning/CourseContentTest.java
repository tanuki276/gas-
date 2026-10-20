package com.example.historymaster.learning;

import static org.junit.Assert.*;

import com.example.historymaster.data.Regions;
import com.example.historymaster.models.QuizQuestion;
import com.google.gson.Gson;
import java.io.*;
import java.util.*;
import org.junit.Test;

public class CourseContentTest {
  @Test
  public void everyRegionHasTwentyDistinctHistoryQuestions() throws Exception {
    try (Reader reader =
        new InputStreamReader(new FileInputStream("src/main/assets/courses.json"), "UTF-8")) {
      Lesson[] lessons = new Gson().fromJson(reader, Lesson[].class);
      assertEquals(14, lessons.length);
      Set<String> ids = new HashSet<>();
      Set<String> prompts = new HashSet<>();
      Set<String> questionIds = new HashSet<>();
      Map<String, Integer> counts = new HashMap<>();
      for (Lesson l : lessons) {
        assertTrue(ids.add(l.id));
        assertNotNull(l.sourceTitle);
        assertFalse(l.sourceTitle.isEmpty());
        assertEquals(10, l.facts.size());
        counts.put(l.region, counts.containsKey(l.region) ? counts.get(l.region) + 1 : 1);
        for (QuizQuestion q : l.questions()) {
          assertTrue("Duplicate prompt: " + q.getQuestion(), prompts.add(q.getQuestion()));
          assertTrue(questionIds.add(q.getId()));
          assertFalse(q.getQuestion().contains("教材で"));
          assertFalse(q.getQuestion().contains("分類され"));
          assertFalse(q.getQuestion().contains("キーワードとして登録"));
          assertEquals(4, q.getOptions().size());
          assertEquals(4, new HashSet<>(q.getOptions()).size());
          assertNotNull(q.getExplanation());
          assertFalse(q.getExplanation().trim().isEmpty());
          assertEquals(l.sourceTitle, q.getSourceTitle());
          assertTrue(q.getCorrectIndex() >= 0 && q.getCorrectIndex() < 4);
        }
      }
      assertEquals(140, prompts.size());
      for (String region : Regions.IDS) {
        assertEquals(2, (int) counts.get(region));
        int total = 0;
        for (Lesson l : lessons) if (region.equals(l.region)) total += l.facts.size();
        assertEquals(20, total);
      }
    }
  }
}
