package com.example.historymaster.learning;

import android.content.Context;
import com.example.historymaster.models.QuizQuestion;
import com.google.gson.Gson;
import java.io.*;
import java.util.*;

public final class LessonRepository {
  private static List<Lesson> cached;

  public static synchronized List<Lesson> all(Context context) {
    if (cached == null) {
      try (Reader reader =
          new InputStreamReader(context.getAssets().open("courses.json"), "UTF-8")) {
        Lesson[] data = new Gson().fromJson(reader, Lesson[].class);
        cached = Collections.unmodifiableList(Arrays.asList(data));
      } catch (Exception e) {
        return Collections.emptyList();
      }
    }
    return cached;
  }

  public static List<Lesson> region(Context context, String region) {
    List<Lesson> result = new ArrayList<>();
    for (Lesson l : all(context)) if (region.equals(l.region)) result.add(l);
    return result;
  }

  public static Lesson find(Context context, String id) {
    for (Lesson l : all(context)) if (l.id.equals(id)) return l;
    return null;
  }

  public static List<QuizQuestion> checkpoint(Context context, String region) {
    List<QuizQuestion> questions = new ArrayList<>();
    for (Lesson l : region(context, region)) questions.addAll(l.questions());
    Collections.shuffle(questions);
    return questions;
  }
}
