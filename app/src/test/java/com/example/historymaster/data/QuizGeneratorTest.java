package com.example.historymaster.data;

import com.example.historymaster.models.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class QuizGeneratorTest {
    private Entity entity(String name, String... tags) {
        Entity e = new Entity();
        e.setName(name);
        e.setTags(Arrays.asList(tags));
        return e;
    }

    private HistoryData data(Entity... entries) {
        HistoryData d = new HistoryData();
        d.setPeriods(Collections.singletonList(new Period("時代", Arrays.asList(entries))));
        return d;
    }

    @Test public void emptyAndSingleEntityReturnWithoutHanging() {
        assertTrue(QuizGenerator.generateQuiz(null, 10).isEmpty());
        assertTrue(QuizGenerator.generateQuiz(data(), 10).isEmpty());
        assertTrue(QuizGenerator.generateQuiz(data(entity("A", "共有")), 10).isEmpty());
        assertTrue(QuizGenerator.generateQuiz(data(entity("A", "共有"), entity("B", "共有")), 10).isEmpty());
    }

    @Test public void distractorsExcludeEveryEntitySharingTheTag() {
        HistoryData d = data(entity("A", "共有"), entity("B", "共有"), entity("C", "別の語"));
        for (int run = 0; run < 50; run++) {
            List<QuizQuestion> qs = QuizGenerator.generateQuiz(d, 10);
            assertEquals(3, qs.size());
            for (QuizQuestion q : qs) {
                assertEquals(q.getOptions().size(), new HashSet<>(q.getOptions()).size());
                assertTrue(q.getCorrectIndex() >= 0);
                assertTrue(q.getCorrectIndex() < q.getOptions().size());
                if (q.getQuestion().contains("共有")) {
                    String correct = q.getOptions().get(q.getCorrectIndex());
                    assertTrue(correct.equals("A") || correct.equals("B"));
                    assertFalse(q.getOptions().contains(correct.equals("A") ? "B" : "A"));
                }
                assertNotNull(q.getExplanation());
            }
        }
    }

    @Test public void nestedEntitiesAreIncludedAndQuestionCountIsBounded() {
        Entity parent = entity("A", "親");
        parent.setSubEntities(Collections.singletonList(entity("B", "子")));
        HistoryData d = data(parent, entity("C", "独立"));
        assertEquals(3, QuizGenerator.generateQuiz(d, 100).size());
        assertEquals(1, QuizGenerator.generateQuiz(d, 1).size());
        assertTrue(QuizGenerator.generateQuiz(d, 0).isEmpty());
    }

    @Test public void allBundledRegionsHavePlayableQuestions() throws Exception {
        for (String region : Regions.IDS) {
            try (java.io.Reader reader = new java.io.InputStreamReader(
                    new java.io.FileInputStream("src/main/assets/" + region + ".json"), "UTF-8")) {
                HistoryData d = new com.google.gson.Gson().fromJson(reader, HistoryData.class);
                assertEquals(region, 10, QuizGenerator.generateQuiz(d, 10).size());
            }
        }
    }
}
