package com.example.historymaster.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import com.example.historymaster.MainActivity;
import com.example.historymaster.design.*;
import com.example.historymaster.learning.*;
import com.example.historymaster.models.QuizQuestion;
import com.example.historymaster.utils.SharedPrefsHelper;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import java.util.*;

public class QuizActivity extends Screen {
  private final Gson gson = new Gson();
  private QuizSession session;
  private String sessionKey, title;
  private boolean review, discardSession;
  private ToneGenerator sound;

  protected boolean checkpoint() {
    return false;
  }

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    review = getIntent().getBooleanExtra("review", false);
    String lessonId = getIntent().getStringExtra("lesson");
    Lesson lesson = LessonRepository.find(this, lessonId);
    if (review) {
      sessionKey = "review";
      title = "発見の復習";
    } else if (checkpoint()) {
      sessionKey = region + ":checkpoint";
      title = "冒険のまとめ";
    } else if (lesson != null) {
      sessionKey = lesson.id;
      region = lesson.region;
      title = lesson.title;
    } else {
      page("レッスンを選んでください", null);
      button("コースへ", this::home, true);
      return;
    }
    String json =
        state != null ? state.getString("session") : ProgressStore.session(this, sessionKey);
    try {
      session = json == null ? null : gson.fromJson(json, QuizSession.class);
    } catch (Exception ignored) {
    }
    if (!valid(session)) session = fresh(lesson);
    save();
    render(false);
  }

  private boolean valid(QuizSession saved) {
    if (saved == null
        || saved.id == null
        || saved.questions == null
        || saved.index < 0
        || saved.index > saved.questions.size()) return false;
    for (QuizQuestion q : saved.questions)
      if (q == null
          || q.getOptions() == null
          || q.getCorrectIndex() < 0
          || q.getCorrectIndex() >= q.getOptions().size()) return false;
    return true;
  }

  private QuizSession fresh(Lesson lesson) {
    QuizSession fresh = new QuizSession();
    if (review) {
      fresh.questions = ProgressStore.review(this);
      if (fresh.questions.size() > 10)
        fresh.questions = new ArrayList<>(fresh.questions.subList(0, 10));
    } else if (checkpoint()) fresh.questions = LessonRepository.checkpoint(this, region);
    else fresh.questions = lesson.questions();
    return fresh;
  }

  private void save() {
    if (!discardSession && session != null && sessionKey != null)
      ProgressStore.saveSession(this, sessionKey, gson.toJson(session));
  }

  private void render(boolean feedbackAnimation) {
    page(title, "途中で閉じても、この端末で続きから再開できます。");
    if (session.questions.isEmpty()) {
      ProgressStore.clearSession(this, sessionKey);
      text(review ? "復習待ちの問題はありません。" : "問題を読み込めませんでした。", 20, true);
      button("コースへ戻る", this::home, true);
      return;
    }
    if (session.finished()) {
      result();
      return;
    }
    QuizQuestion question = session.questions.get(session.index);
    text("QUESTION " + (session.index + 1) + " / " + session.questions.size(), 13, true);
    meter(
        body,
        session.index,
        session.questions.size(),
        "全" + session.questions.size() + "問中、" + session.index + "問を完了");
    LinearLayout questionCard = card(Palette.LAVENDER);
    label(questionCard, question.getQuestion(), 23, Palette.INK, true);
    text(session.answered ? "回答を確認しよう" : "答えをひとつ選んでください", 14, false);
    List<MaterialButton> options = new ArrayList<>();
    final Button[] submitHolder = new Button[1];
    for (int i = 0; i < question.getOptions().size(); i++) {
      final int option = i;
      MaterialButton choice =
          action(
              body,
              (i + 1) + "   " + question.getOptions().get(i),
              () -> {
                if (session.answered) return;
                session.selected = option;
                save();
                for (int k = 0; k < options.size(); k++) styleChoice(options.get(k), k, question);
                submitHolder[0].setEnabled(true);
              },
              false);
      choice.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
      choice.setEnabled(!session.answered);
      options.add(choice);
      styleChoice(choice, i, question);
    }
    if (!session.answered) {
      submitHolder[0] =
          button(
              "答え合わせ",
              () -> {
                if (!session.answer(session.selected)) return;
                boolean correct = question.checkAnswer(session.selected);
                ProgressStore.recordAnswer(this, question, correct);
                save();
                playTone(correct);
                render(true);
              },
              true);
      submitHolder[0].setEnabled(session.selected >= 0);
    } else {
      boolean correct = question.checkAnswer(session.selected);
      LinearLayout feedback = card(correct ? Palette.MINT : 0xFFFFF1DD);
      label(
          feedback,
          correct ? "✓  その通り！" : "ここで覚えれば、大丈夫。",
          22,
          correct ? Palette.GREEN : Palette.INK,
          true);
      if (!correct)
        label(
            feedback,
            "正解：" + question.getOptions().get(question.getCorrectIndex()),
            18,
            Palette.INK,
            true);
      label(feedback, question.getExplanation(), 16, Palette.INK, false);
      if (question.getSourceTitle() != null)
        action(
            feedback,
            "出典・詳しい解説を読む ↗",
            () -> article(question.getRegion(), question.getSourceTitle()),
            false);
      button(
          session.index + 1 == session.questions.size() ? "レッスンを完了する" : "次の発見へ  →",
          () -> {
            if (session.next()) {
              save();
              render(false);
            }
          },
          true);
      if (feedbackAnimation) {
        Motion.pop(feedback);
        feedback.announceForAccessibility(
            correct ? "正解です" : "正解は" + question.getOptions().get(question.getCorrectIndex()));
        android.widget.ScrollView targetScroll = scroll;
        targetScroll.post(
            () -> {
              if (targetScroll != scroll || !feedback.isAttachedToWindow()) return;
              if (Motion.enabled(this)) targetScroll.smoothScrollTo(0, feedback.getTop());
              else targetScroll.scrollTo(0, feedback.getTop());
            });
      }
    }
  }

  private void styleChoice(MaterialButton button, int index, QuizQuestion question) {
    boolean selected = session.selected == index;
    int color = selected ? Palette.LAVENDER : Palette.WHITE;
    if (session.answered && index == question.getCorrectIndex()) color = Palette.MINT;
    button.setBackgroundTintList(ColorStateList.valueOf(color));
    button.setTextColor(Palette.INK);
    button.setStrokeColor(ColorStateList.valueOf(selected ? Palette.PRIMARY : Palette.BORDER));
    button.setStrokeWidth(dp(selected ? 2 : 1));
    button.setSelected(selected);
    String marker =
        session.answered && index == question.getCorrectIndex() ? "✓  " : (index + 1) + "   ";
    button.setText(marker + question.getOptions().get(index));
    button.setContentDescription(
        question.getOptions().get(index)
            + (selected ? "、選択中" : "")
            + (session.answered && index == question.getCorrectIndex() ? "、正解" : ""));
  }

  private void result() {
    boolean first = !session.rewardApplied;
    if (first) {
      session.earned =
          ProgressStore.complete(
              this, session.id, sessionKey, region, session.score, session.questions.size());
      session.rewardApplied = true;
      SharedPrefsHelper.saveQuizScore(this, region, session.score * 100 / session.questions.size());
      save();
    }
    LinearLayout result = card(Palette.LAVENDER);
    mascot(result, 150, true);
    label(
        result,
        session.score == session.questions.size() ? "パーフェクト！" : "ひとつ、世界が広がった。",
        25,
        Palette.INK,
        true);
    label(
        result,
        session.score + " / " + session.questions.size() + "問正解",
        24,
        Palette.PRIMARY,
        true);
    label(
        result,
        session.earned > 0 ? "✦  +" + session.earned + " XP" : "今日のXPは獲得済み。復習も、立派な一歩。",
        18,
        Palette.INK,
        true);
    boolean passed = session.score * 3 >= session.questions.size() * 2;
    if (!review)
      label(
          result,
          passed ? "✓ このステップをクリアしました。" : "あと少し！ 解説を読み直して、もう一度挑戦できます。",
          15,
          Palette.MUTED,
          false);
    LearningState progress = ProgressStore.load(this);
    int daily = progress.xpToday(DayKey.today()), goal = ProgressStore.goal(this);
    meter(result, daily, goal, "今日の目標 " + daily + " / " + goal + " XP");
    label(result, "今日の目標 " + daily + " / " + goal + " XP", 14, Palette.MUTED, false);
    button(
        "コースへ戻る",
        () -> {
          discardSession = true;
          ProgressStore.clearSession(this, sessionKey);
          home();
        },
        true);
    button(
        review ? "残っている問題を復習" : "もう一度挑戦する",
        () -> {
          session = fresh(LessonRepository.find(this, sessionKey));
          save();
          render(false);
        },
        false);
    if (first && session.earned > 0) celebrate();
  }

  private void home() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
    finish();
  }

  private void playTone(boolean correct) {
    if (!ProgressStore.setting(this, "sound", false)) return;
    try {
      if (sound == null) sound = new ToneGenerator(AudioManager.STREAM_MUSIC, 35);
      sound.startTone(correct ? ToneGenerator.TONE_PROP_ACK : ToneGenerator.TONE_PROP_BEEP, 110);
    } catch (RuntimeException ignored) {
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);
    if (session != null) state.putString("session", gson.toJson(session));
  }

  @Override
  protected void onStop() {
    save();
    if (sound != null) {
      sound.release();
      sound = null;
    }
    super.onStop();
  }
}
