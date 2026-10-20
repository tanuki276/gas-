package com.example.historymaster.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.example.historymaster.design.Palette;
import com.example.historymaster.learning.*;

public class LearningActivity extends Screen {
  private Lesson lesson;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    lesson = LessonRepository.find(this, getIntent().getStringExtra("lesson"));
    if (lesson == null) {
      page("レッスンが見つかりません", null);
      button("地域の解説を読む", () -> open(ExplanationActivity.class), true);
      return;
    }
    region = lesson.region;
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (lesson != null) render();
  }

  private void render() {
    page(lesson.title, lesson.subtitle + "  /  ミニレッスン");
    LinearLayout header = card(Palette.LAVENDER);
    int count = lesson.facts.size();
    int passCount = (count * 2 + 2) / 3;
    label(header, "まずは、" + count + "の発見。", 23, Palette.INK, true);
    label(header, "読み終えたら" + count + "問に挑戦。" + passCount + "問以上正解でクリアできます。", 15, Palette.MUTED, false);
    int n = 1;
    for (Lesson.Fact fact : lesson.facts) {
      LinearLayout item = card(Palette.WHITE);
      label(item, "DISCOVERY " + n++, 12, Palette.PRIMARY, true);
      label(item, fact.answer, 23, Palette.INK, true);
      label(item, fact.note, 16, Palette.MUTED, false);
    }
    button(
        ProgressStore.session(this, lesson.id) == null ? "わかった！ " + count + "問に挑戦" : "途中のクイズを再開",
        () -> {
          Intent intent = new Intent(this, QuizActivity.class);
          intent.putExtra("region", region);
          intent.putExtra("lesson", lesson.id);
          startActivity(intent);
        },
        true);
    button("Wikipediaでもっと詳しく", () -> article(lesson.sourceTitle), false);
    text(
        "出典：日本語版Wikipedia「"
            + lesson.sourceTitle
            + "」。基本的な事実を学習用に短く構成しています。記事の投稿者と編集履歴は出典画面から確認できます。",
        12,
        false);
    text("初回の完了で20 XP＋正解1問につき5 XP。同じレッスンのXP獲得は1日1回です。", 12, false);
  }
}
