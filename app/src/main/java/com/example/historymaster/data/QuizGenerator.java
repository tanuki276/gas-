package com.example.historymaster.data;

import com.example.historymaster.models.*;
import java.util.*;

public class QuizGenerator {
    private static final Random random = new Random();

    public static List<QuizQuestion> generateQuiz(HistoryData data, int questionCount) {
        List<QuizQuestion> pool = new ArrayList<>();
        if (data == null || data.getPeriods() == null || questionCount <= 0) return pool;

        // Merge by name so duplicate entries never become contradictory distractors.
        Map<String, Set<String>> tagsByName = new LinkedHashMap<>();
        Map<String, Set<String>> periodsByName = new LinkedHashMap<>();
        for (Period period : data.getPeriods()) {
            if (period != null) {
                collect(period.getMiddleLevelEntities(), period.getUpperLevel(),
                        tagsByName, periodsByName);
            }
        }
        Set<String> allTags = new LinkedHashSet<>();
        for (Set<String> tags : tagsByName.values()) allTags.addAll(tags);
        Set<String> allPeriods = new LinkedHashSet<>();
        for (Set<String> periods : periodsByName.values()) allPeriods.addAll(periods);

        for (Map.Entry<String, Set<String>> entry : tagsByName.entrySet()) {
            String name = entry.getKey();
            Set<String> tags = entry.getValue();
            if (!tags.isEmpty()) {
                List<String> answers = new ArrayList<>(tags);
                Collections.shuffle(answers, random);
                String tag = answers.get(0);
                Set<String> wrongTags = new LinkedHashSet<>(allTags);
                wrongTags.removeAll(tags);
                addChoice(pool, "学習データで「" + name + "」に関連するキーワードはどれですか？",
                        tag, wrongTags, "multiple_choice", name + "：" + tag);
            }
            Set<String> periods = periodsByName.get(name);
            for (String period : periods) {
                Set<String> wrongPeriods = new LinkedHashSet<>(allPeriods);
                wrongPeriods.removeAll(periods);
                addChoice(pool, "学習データで「" + name + "」が分類されている時代区分はどれですか？",
                        period, wrongPeriods, "multiple_choice", name + "：" + period);
            }
        }
        // A shared keyword may have multiple owners. All owners are excluded from distractors.
        for (String tag : allTags) {
            List<String> owners = new ArrayList<>();
            Set<String> wrongNames = new LinkedHashSet<>();
            for (Map.Entry<String, Set<String>> entry : tagsByName.entrySet()) {
                if (entry.getValue().contains(tag)) owners.add(entry.getKey());
                else wrongNames.add(entry.getKey());
            }
            Collections.shuffle(owners, random);
            addChoice(pool, "学習データで「" + tag + "」がキーワードとして登録されているのはどれですか？",
                    owners.get(0), wrongNames, "multiple_choice",
                    "「" + tag + "」の登録先：" + joinNames(owners));
        }
        Collections.shuffle(pool, random);
        // Return each prompt at most once, even if the requested count exceeds the pool.
        Map<String, QuizQuestion> unique = new LinkedHashMap<>();
        for (QuizQuestion question : pool) unique.putIfAbsent(question.getQuestion(), question);
        List<QuizQuestion> questions = new ArrayList<>(unique.values());
        return new ArrayList<>(questions.subList(0, Math.min(questionCount, questions.size())));
    }

    private static void collect(List<Entity> entities, String period,
            Map<String, Set<String>> tagsByName, Map<String, Set<String>> periodsByName) {
        if (entities == null) return;
        for (Entity entity : entities) {
            if (entity == null) continue;
            String name = entity.getName();
            if (name != null && !name.trim().isEmpty()) {
                Set<String> tags = tagsByName.computeIfAbsent(name, key -> new LinkedHashSet<>());
                if (entity.getTags() != null) {
                    for (String tag : entity.getTags()) {
                        if (tag != null && !tag.trim().isEmpty()) tags.add(tag);
                    }
                }
                Set<String> periods = periodsByName.computeIfAbsent(name, key -> new LinkedHashSet<>());
                if (period != null && !period.trim().isEmpty()) periods.add(period);
            }
            collect(entity.getSubEntities(), period, tagsByName, periodsByName);
        }
    }

    private static String joinNames(List<String> names) {
        StringBuilder result = new StringBuilder();
        for (String name : names) {
            if (result.length() > 0) result.append("、");
            result.append(name);
        }
        return result.toString();
    }

    private static void addChoice(List<QuizQuestion> pool, String prompt, String answer,
            Set<String> distractors, String type, String explanation) {
        if (distractors.isEmpty()) return;
        List<String> wrong = new ArrayList<>(distractors);
        Collections.shuffle(wrong, random);
        List<String> options = new ArrayList<>();
        options.add(answer);
        options.addAll(wrong.subList(0, Math.min(3, wrong.size())));
        Collections.shuffle(options, random);
        QuizQuestion question = new QuizQuestion(prompt, options, options.indexOf(answer), type);
        question.setExplanation(explanation);
        pool.add(question);
    }
}
