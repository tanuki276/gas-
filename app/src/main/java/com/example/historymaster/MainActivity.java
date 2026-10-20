package com.example.historymaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import com.example.historymaster.data.*;
import com.example.historymaster.design.*;
import com.example.historymaster.learning.*;
import com.example.historymaster.models.QuizQuestion;
import com.example.historymaster.ui.*;
import com.example.historymaster.utils.SharedPrefsHelper;
import com.google.android.material.button.MaterialButton;
import java.util.*;

public class MainActivity extends Screen {
  private int tab;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    region = Regions.safe(SharedPrefsHelper.getCurrentRegion(this, "japan"));
    if (state != null) tab = state.getInt("tab", 0);
  }

  @Override
  protected void onResume() {
    super.onResume();
    render();
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);
    state.putInt("tab", tab);
  }

  private void render() {
    page("Jipains", "小さな発見が、世界を広げる。");
    LearningState state = ProgressStore.load(this);
    text(
        "✦  "
            + state.xp
            + " XP     ·     "
            + state.visibleStreak(DayKey.today())
            + "日連続     ·     Lv. "
            + state.level(),
        14,
        true);
    if (tab == 0) study(state);
    else if (tab == 1) explore();
    else if (tab == 2) review();
    else passport(state);
    navigation();
  }

  private void regionPicker() {
    text("旅する地域", 14, true);
    HorizontalScrollView scroller = new HorizontalScrollView(this);
    scroller.setHorizontalScrollBarEnabled(false);
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    scroller.addView(row);
    body.addView(scroller);
    for (int i = 0; i < Regions.IDS.length; i++) {
      final String id = Regions.IDS[i];
      MaterialButton chip =
          action(
              row,
              Regions.NAMES[i],
              () -> {
                region = id;
                SharedPrefsHelper.saveCurrentRegion(this, id);
                render();
              },
              region.equals(id));
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
      lp.setMargins(0, dp(4), dp(8), dp(4));
      chip.setLayoutParams(lp);
      chip.setMinHeight(dp(48));
      chip.setTextSize(14);
      chip.setSelected(region.equals(id));
    }
  }

  private void study(LearningState state) {
    regionPicker();
    LinearLayout hero = card(Palette.LAVENDER);
    mascot(hero, 112, false);
    label(hero, "ジピと、歴史の冒険へ。", 23, Palette.INK, true);
    label(hero, "読む → 3問に挑戦 → 発見を自分のものに。", 14, Palette.MUTED, false);
    List<Lesson> lessons = LessonRepository.region(this, region);
    if (lessons.isEmpty()) {
      text("コースを読み込めませんでした。図鑑から記事を探せます。", 16, false);
      return;
    }
    Lesson next = lessons.get(0);
    for (Lesson lesson : lessons)
      if (!state.mastered.contains(lesson.id)) {
        next = lesson;
        break;
      }
    final Lesson destination = next;
    action(
        hero,
        state.mastered.containsAll(ids(lessons)) ? "もう一度、冒険する" : "レッスンを続ける  →",
        () -> lesson(destination.id),
        true);
    Motion.enter(hero);
    LinearLayout goal = card(Palette.WHITE);
    int today = state.xpToday(DayKey.today()), target = ProgressStore.goal(this);
    label(goal, "今日の目標     " + today + " / " + target + " XP", 17, Palette.INK, true);
    meter(goal, today, target, "今日のXP " + today + "、目標 " + target);
    label(
        goal,
        today >= target ? "✓ 今日の目標を達成！ 自分のペースで続けよう。" : "少しずつで大丈夫。まずは1レッスン。",
        13,
        Palette.MUTED,
        false);
    text(Regions.name(region) + "の冒険コース", 23, true);
    text("2つのレッスンとまとめ。3問中2問の正解で次へ進めます。", 14, false);
    boolean unlocked = true;
    for (int i = 0; i < lessons.size(); i++) {
      Lesson lesson = lessons.get(i);
      boolean done = state.mastered.contains(lesson.id);
      LinearLayout node = card(done ? Palette.MINT : Palette.WHITE);
      label(
          node,
          (done ? "✓ CLEAR" : "STEP 0" + (i + 1)) + "   ·   " + lesson.subtitle,
          12,
          done ? Palette.GREEN : Palette.PRIMARY,
          true);
      label(node, lesson.title, 21, Palette.INK, true);
      Button start =
          action(
              node,
              done ? "もう一度学ぶ  ↗" : unlocked ? "このレッスンを開く" : "前のレッスンをクリアすると解放",
              () -> lesson(lesson.id),
              !done && unlocked);
      start.setEnabled(unlocked || done);
      if (i < lessons.size() - 1) {
        TextView connector = text("●\n│", 12, false);
        connector.setGravity(Gravity.CENTER);
        connector.setTextColor(Palette.BORDER);
      }
      unlocked = done;
    }
    boolean checkpointOpen = state.mastered.containsAll(ids(lessons));
    LinearLayout checkpoint = card(Palette.LAVENDER);
    label(checkpoint, "★  CHECKPOINT", 13, Palette.PRIMARY, true);
    label(checkpoint, "冒険のまとめ · 6問", 21, Palette.INK, true);
    Button finish =
        action(
            checkpoint,
            checkpointOpen ? "まとめに挑戦" : "2レッスンをクリアして解放",
            () -> open(TestActivity.class),
            true);
    finish.setEnabled(checkpointOpen);
  }

  private List<String> ids(List<Lesson> lessons) {
    List<String> result = new ArrayList<>();
    for (Lesson l : lessons) result.add(l.id);
    return result;
  }

  private void lesson(String id) {
    Intent intent = new Intent(this, LearningActivity.class);
    intent.putExtra("region", region);
    intent.putExtra("lesson", id);
    startActivity(intent);
  }

  private void explore() {
    text("世界の図鑑", 27, true);
    text("知りたい国や王朝を、自由に探そう。", 15, false);
    button("全7地域をまとめて検索", () -> open(SearchActivity.class), true);
    regionPicker();
    LinearLayout card = card(Palette.WHITE);
    label(card, Regions.name(region) + "の歴史", 23, Palette.INK, true);
    label(card, "まずは全体の流れをつかむ。Wikipediaの概説へ。", 15, Palette.MUTED, false);
    action(card, "歴史の解説を読む", () -> article(Regions.ARTICLES[Regions.index(region)]), true);
    action(card, "時代・国・王朝から探す", () -> open(ExplanationActivity.class), false);
    text("お気に入り", 22, true);
    List<String> favorites = new ArrayList<>(SharedPrefsHelper.favorites(this));
    Collections.sort(favorites);
    if (favorites.isEmpty()) text("気になった記事の「保存」を押すと、ここに並びます。", 15, false);
    for (String key : favorites) {
      String[] parts = key.split("\u001f", 2);
      if (parts.length != 2) continue;
      button(
          "★ " + parts[1] + "  ·  " + Regions.name(parts[0]),
          () -> article(parts[0], parts[1]),
          false);
    }
  }

  private void review() {
    List<QuizQuestion> questions = ProgressStore.review(this);
    text("間違いは、次の発見。", 27, true);
    LinearLayout card = card(Palette.MINT);
    mascot(card, 118, true);
    label(
        card,
        questions.isEmpty() ? "復習待ちはありません" : "復習したい問題が " + questions.size() + "問",
        22,
        Palette.INK,
        true);
    label(
        card,
        questions.isEmpty() ? "レッスンで覚えたことを試してみよう。" : "正解すると復習リストから外れます。何度でも挑戦できます。",
        15,
        Palette.MUTED,
        false);
    if (!questions.isEmpty())
      action(
          card,
          "復習をはじめる",
          () -> {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("region", region);
            intent.putExtra("review", true);
            startActivity(intent);
          },
          true);
    else
      action(
          card,
          "冒険コースへ",
          () -> {
            tab = 0;
            render();
          },
          true);
    for (QuizQuestion q : questions) {
      LinearLayout item = card(Palette.WHITE);
      label(item, Regions.name(q.getRegion()), 12, Palette.PRIMARY, true);
      label(item, q.getQuestion(), 17, Palette.INK, true);
      if (q.getSourceTitle() != null)
        action(item, "解説で予習する", () -> article(q.getRegion(), q.getSourceTitle()), false);
    }
  }

  private void passport(LearningState state) {
    text("冒険のパスポート", 27, true);
    LinearLayout profile = card(Palette.LAVENDER);
    mascot(profile, 112, state.sessions > 0);
    label(
        profile,
        "Lv. " + state.level() + "  ·  " + (state.level() < 3 ? "歴史の旅人" : "発見の探究者"),
        22,
        Palette.INK,
        true);
    label(
        profile,
        state.xp + " XP  /  完了 " + state.sessions + "回  /  最長 " + state.longestStreak + "日連続",
        14,
        Palette.MUTED,
        false);
    meter(profile, state.xp % 100, 100, "次のレベルまで " + (100 - state.xp % 100) + " XP");
    label(profile, "次のレベルまで " + (100 - state.xp % 100) + " XP", 13, Palette.MUTED, false);
    text("今日のミッション", 22, true);
    boolean today = DayKey.today().equals(state.activeDay);
    mission("レッスンを1回完了", today && state.dailyLessons >= 1);
    mission("新しい解説を1つ読了", today && state.dailyReads >= 1);
    mission(
        ProgressStore.goal(this) + " XPを獲得",
        state.xpToday(DayKey.today()) >= ProgressStore.goal(this));
    text("発見のバッジ", 22, true);
    badge("はじめの一歩", "レッスンを1回完了", state.sessions >= 1);
    badge("ナイスパーフェクト", "1回のレッスンで全問正解", state.perfectSessions >= 1);
    badge("3日間の旅", "3日連続で学習", state.longestStreak >= 3);
    badge("国境を越えて", "3つの地域でレッスンを完了", state.visitedRegions.size() >= 3);
    badge("7地域の探究者", "すべての地域でレッスンを完了", state.visitedRegions.size() >= 7);
    button("目標・動き・効果音の設定", () -> open(SettingsActivity.class), false);
    text("学習記録はこの端末に保存されます。", 12, false);
  }

  private void mission(String title, boolean done) {
    text((done ? "✓  " : "○  ") + title, 16, done);
  }

  private void badge(String title, String condition, boolean earned) {
    LinearLayout card = card(earned ? Palette.MINT : Palette.WHITE);
    label(card, (earned ? "★  " : "☆  ") + title, 18, earned ? Palette.GREEN : Palette.INK, true);
    label(card, condition + (earned ? " · 達成済み" : " · これから"), 13, Palette.MUTED, false);
  }

  private void navigation() {
    LinearLayout nav = new LinearLayout(this);
    nav.setOrientation(LinearLayout.HORIZONTAL);
    nav.setPadding(dp(8), dp(8), dp(8), dp(8));
    nav.setBackgroundColor(Palette.WHITE);
    String[] titles = {"学ぶ", "図鑑", "復習", "記録"};
    for (int i = 0; i < titles.length; i++) {
      final int target = i;
      MaterialButton button =
          action(
              nav,
              titles[i],
              () -> {
                tab = target;
                render();
              },
              false);
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
      lp.setMargins(dp(2), 0, dp(2), 0);
      button.setLayoutParams(lp);
      button.setTextSize(14);
      button.setPadding(dp(4), dp(8), dp(4), dp(8));
      button.setMinWidth(0);
      button.setMinimumWidth(0);
      button.setBackgroundTintList(
          android.content.res.ColorStateList.valueOf(i == tab ? Palette.LAVENDER : Palette.WHITE));
      button.setTextColor(i == tab ? Palette.PRIMARY : Palette.MUTED);
      button.setStrokeWidth(0);
      button.setSelected(i == tab);
      button.setContentDescription(titles[i] + (i == tab ? "、選択中" : ""));
    }
    shell.addView(nav, new LinearLayout.LayoutParams(-1, -2));
  }
}
