package com.example.historymaster.data;

import com.example.historymaster.models.Entity;
import com.example.historymaster.models.HistoryData;
import com.example.historymaster.models.Period;
import com.example.historymaster.models.QuizQuestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class QuizGenerator {

    private static final Random RANDOM = new Random();

    private QuizGenerator() {
    }

    public static List<QuizQuestion> generateQuiz(HistoryData data, int questionCount) {
        List<QuizQuestion> questions = new ArrayList<>();
        List<Entity> allEntities = getAllEntities(data);
        if (allEntities.isEmpty() || questionCount <= 0) return questions;

        int maxQuestions = Math.min(questionCount, Math.max(1, allEntities.size() * 2));
        for (int i = 0; i < maxQuestions; i++) {
            QuizQuestion question;
            if (allEntities.size() >= 4 && i % 2 == 0) {
                question = generateMultipleChoice(allEntities);
            } else {
                question = generateTrueFalse(allEntities);
            }
            if (question != null) questions.add(question);
        }
        return questions;
    }

    private static QuizQuestion generateMultipleChoice(List<Entity> allEntities) {
        if (allEntities.size() < 2) return generateTrueFalse(allEntities);

        Entity correctEntity = allEntities.get(RANDOM.nextInt(allEntities.size()));
        String question = String.format("「%s」について正しい説明はどれですか？", safeName(correctEntity));
        String correctAnswer = generateCorrectDescription(correctEntity);

        List<Entity> candidates = new ArrayList<>(allEntities);
        candidates.remove(correctEntity);
        Collections.shuffle(candidates, RANDOM);

        Set<String> optionSet = new LinkedHashSet<>();
        optionSet.add(correctAnswer);
        for (Entity candidate : candidates) {
            if (optionSet.size() >= 4) break;
            optionSet.add(generateDescription(candidate));
        }
        while (optionSet.size() < Math.min(4, allEntities.size())) {
            optionSet.add("該当する説明はありません " + optionSet.size());
        }

        List<String> options = new ArrayList<>(optionSet);
        Collections.shuffle(options, RANDOM);
        int correctIndex = options.indexOf(correctAnswer);
        QuizQuestion result = new QuizQuestion(question, options, correctIndex, "multiple_choice");
        result.setExplanation(correctAnswer);
        return result;
    }

    private static QuizQuestion generateTrueFalse(List<Entity> allEntities) {
        if (allEntities == null || allEntities.isEmpty()) return null;

        Entity entity = allEntities.get(RANDOM.nextInt(allEntities.size()));
        boolean isTrue = RANDOM.nextBoolean();
        String trueFact = generateCorrectDescription(entity);
        String statement = isTrue ? trueFact : generateFalseStatement(entity, allEntities);
        List<String> options = Arrays.asList("正しい", "間違い");
        QuizQuestion result = new QuizQuestion(
            "次の文は正しいですか？\n\n" + statement,
            options,
            isTrue ? 0 : 1,
            "true_false"
        );
        result.setExplanation(trueFact);
        return result;
    }

    private static String generateFalseStatement(Entity entity, List<Entity> allEntities) {
        if (allEntities.size() > 1) {
            List<Entity> others = new ArrayList<>(allEntities);
            others.remove(entity);
            Entity other = others.get(RANDOM.nextInt(others.size()));
            String otherTag = firstTag(other);
            if (otherTag != null) {
                return safeName(entity) + "は「" + otherTag + "」で知られています";
            }
        }
        return safeName(entity) + "は歴史上存在しなかった国家・勢力です";
    }

    private static List<Entity> getAllEntities(HistoryData data) {
        List<Entity> allEntities = new ArrayList<>();
        if (data == null || data.getPeriods() == null) return allEntities;
        for (Period period : data.getPeriods()) {
            if (period != null) collectEntities(period.getMiddleLevelEntities(), allEntities);
        }
        return allEntities;
    }

    private static void collectEntities(List<Entity> entities, List<Entity> result) {
        if (entities == null) return;
        for (Entity entity : entities) {
            if (entity == null || safeName(entity).isEmpty()) continue;
            result.add(entity);
            collectEntities(entity.getSubEntities(), result);
        }
    }

    private static String generateCorrectDescription(Entity entity) {
        String tag = firstTag(entity);
        if (tag != null) return safeName(entity) + "は「" + tag + "」で知られています";
        return safeName(entity) + "はこの地域の歴史に登場する国家・勢力です";
    }

    private static String generateDescription(Entity entity) {
        String tag = firstTag(entity);
        return tag == null
            ? safeName(entity) + "はこの地域の歴史に登場する国家・勢力です"
            : safeName(entity) + "は「" + tag + "」で知られています";
    }

    private static String firstTag(Entity entity) {
        if (entity == null || entity.getTags() == null || entity.getTags().isEmpty()) return null;
        String tag = entity.getTags().get(0);
        return tag == null || tag.trim().isEmpty() ? null : tag.trim();
    }

    private static String safeName(Entity entity) {
        return entity == null || entity.getName() == null ? "" : entity.getName().trim();
    }
}
