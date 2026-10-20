package com.example.historymaster.ui;

import android.os.Bundle;
import android.widget.LinearLayout;
import com.example.historymaster.data.Regions;
import com.example.historymaster.design.Palette;
import com.example.historymaster.models.*;

public class ExplanationActivity extends Screen {
  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    page(Regions.name(region) + "の歴史図鑑", "時代をひらいて、国・王朝・出来事を探しましょう。");
    button("まずは歴史の概説を読む", () -> article(Regions.ARTICLES[Regions.index(region)]), true);
    if (!ready()) return;
    text("教材の分類・関連語は既存の資料に基づきます。各項目の詳しい説明と出典はWikipediaで確認できます。", 13, false);
    for (Period period : data.getPeriods()) {
      LinearLayout section = card(Palette.WHITE);
      final LinearLayout list = new LinearLayout(this);
      list.setOrientation(LinearLayout.VERTICAL);
      list.setVisibility(android.view.View.GONE);
      action(
          section,
          "＋  " + period.getUpperLevel(),
          () -> {
            if (list.getChildCount() == 0) show(list, period.getMiddleLevelEntities(), 0);
            boolean expanded = list.getVisibility() == android.view.View.VISIBLE;
            list.setVisibility(expanded ? android.view.View.GONE : android.view.View.VISIBLE);
          },
          false);
      section.addView(list);
    }
  }

  private void show(LinearLayout parent, java.util.List<Entity> entities, int depth) {
    if (entities == null) return;
    for (Entity entity : entities) {
      action(
          parent,
          (depth > 0 ? "↳  " : "") + entity.getName(),
          () -> article(entity.getName()),
          false);
      if (entity.getTags() != null)
        label(
            parent, android.text.TextUtils.join(" · ", entity.getTags()), 12, Palette.MUTED, false);
      show(parent, entity.getSubEntities(), depth + 1);
    }
  }
}
