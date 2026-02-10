package com.historymaster.data;

import com.historymaster.models.*;
import java.util.*;

public class QuizGenerator {
    
    private static final Random random = new Random();
    
    public static List<QuizQuestion> generateQuiz(HistoryData data, int questionCount) {
        List<QuizQuestion> questions = new ArrayList<>();
        List<Entity> allEntities = getAllEntities(data);
        
        if (allEntities.isEmpty()) return questions;
        
        for (int i = 0; i < questionCount; i++) {
            int type = random.nextInt(3); // 0-2: 3種類の問題
            
            switch (type) {
                case 0:
                    questions.add(generateMultipleChoice(allEntities));
                    break;
                case 1:
                    questions.add(generateTrueFalse(allEntities));
                    break;
                case 2:
                    questions.add(generateTimelineQuestion(allEntities));
                    break;
            }
        }
        
        return questions;
    }
    
    private static QuizQuestion generateMultipleChoice(List<Entity> allEntities) {
        Entity correctEntity = allEntities.get(random.nextInt(allEntities.size()));
        
        String question = String.format("「%s」について正しい説明はどれですか？", correctEntity.getName());
        String correctAnswer = generateCorrectDescription(correctEntity);
        
        // 誤答を3つ生成
        List<String> wrongAnswers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Entity wrongEntity;
            do {
                wrongEntity = allEntities.get(random.nextInt(allEntities.size()));
            } while (wrongEntity == correctEntity);
            wrongAnswers.add(generateWrongDescription(wrongEntity));
        }
        
        // 選択肢をまとめてシャッフル
        List<String> allOptions = new ArrayList<>();
        allOptions.add(correctAnswer);
        allOptions.addAll(wrongAnswers);
        Collections.shuffle(allOptions);
        
        int correctIndex = allOptions.indexOf(correctAnswer);
        
        return new QuizQuestion(question, allOptions, correctIndex, "multiple_choice");
    }
    
    private static QuizQuestion generateTrueFalse(List<Entity> allEntities) {
        Entity entity = allEntities.get(random.nextInt(allEntities.size()));
        boolean isTrue = random.nextBoolean();
        
        String fact = generateFact(entity);
        String statement = isTrue ? fact : modifyFact(fact);
        
        String question = "次の文は正しいですか？\n\n" + statement;
        List<String> options = Arrays.asList("正しい", "間違い");
        int correctIndex = isTrue ? 0 : 1;
        
        return new QuizQuestion(question, options, correctIndex, "true_false");
    }
    
    private static QuizQuestion generateTimelineQuestion(List<Entity> allEntities) {
        // 3つのエンティティを選択
        List<Entity> selected = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Entity entity;
            do {
                entity = allEntities.get(random.nextInt(allEntities.size()));
            } while (selected.contains(entity));
            selected.add(entity);
        }
        
        // 時代順に並べ替え（簡易的）
        selected.sort((a, b) -> estimateEra(a) - estimateEra(b));
        
        StringBuilder question = new StringBuilder();
        question.append("以下の王朝を、成立したのが古い順に並べてください：\n\n");
        
        List<String> shuffledNames = new ArrayList<>();
        for (Entity entity : selected) {
            shuffledNames.add(entity.getName());
        }
        Collections.shuffle(shuffledNames);
        
        for (int i = 0; i < shuffledNames.size(); i++) {
            question.append(i + 1).append(". ").append(shuffledNames.get(i)).append("\n");
        }
        
        return new QuizQuestion(question.toString(), shuffledNames, 0, "timeline_order");
    }
    
    // ヘルパーメソッド
    private static List<Entity> getAllEntities(HistoryData data) {
        List<Entity> allEntities = new ArrayList<>();
        for (Period period : data.getPeriods()) {
            collectEntities(period.getMiddleLevelEntities(), allEntities);
        }
        return allEntities;
    }
    
    private static void collectEntities(List<Entity> entities, List<Entity> result) {
        for (Entity entity : entities) {
            result.add(entity);
            if (entity.getSubEntities() != null) {
                collectEntities(entity.getSubEntities(), result);
            }
        }
    }
    
    private static String generateCorrectDescription(Entity entity) {
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            return entity.getName() + "は" + entity.getTags().get(0) + "でした";
        }
        return entity.getName() + "は重要な王朝でした";
    }
    
    private static String generateWrongDescription(Entity entity) {
        return entity.getName() + "は" + getRandomWrongDescription() + "でした";
    }
    
    private static String getRandomWrongDescription() {
        String[] wrongs = {"存在しなかった", "別の地域の", "神話上の", "後に創作された"};
        return wrongs[random.nextInt(wrongs.length)];
    }
    
    private static String generateFact(Entity entity) {
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            return entity.getName() + "は" + entity.getTags().get(0) + "です";
        }
        return entity.getName() + "は歴史的に重要です";
    }
    
    private static String modifyFact(String fact) {
        String[] modifiers = {"ではなかった", "の逆である", "とは関係ない"};
        return fact.replace("です", modifiers[random.nextInt(modifiers.length)] + "です");
    }
    
    private static int estimateEra(Entity entity) {
        // 簡易的な時代推定
        String name = entity.getName();
        if (name.contains("夏") || name.contains("殷")) return 1;
        if (name.contains("秦") || name.contains("漢")) return 2;
        if (name.contains("唐") || name.contains("宋")) return 3;
        if (name.contains("明") || name.contains("清")) return 4;
        return 5;
    }
}